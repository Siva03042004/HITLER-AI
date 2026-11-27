const crypto = require("crypto");
const bcrypt = require("bcryptjs");
const { ObjectId } = require("mongodb");
const { connectToMongoDB } = require("./db");

const COOKIE_NAME = "hitler_ai_session";
const SESSION_DAYS = 7;

function hashToken(token) {
  return crypto.createHash("sha256").update(token).digest("hex");
}

function readCookie(req, name) {
  const header = req.headers.cookie || "";
  for (const part of header.split(";")) {
    const index = part.indexOf("=");
    if (index < 0) continue;
    if (part.slice(0, index).trim() === name) {
      return decodeURIComponent(part.slice(index + 1).trim());
    }
  }
  return null;
}

function setSessionCookie(res, token) {
  const parts = [
    `${COOKIE_NAME}=${encodeURIComponent(token)}`,
    "Path=/",
    "HttpOnly",
    "SameSite=Lax",
    `Max-Age=${SESSION_DAYS * 24 * 60 * 60}`
  ];
  if (process.env.NODE_ENV === "production") parts.push("Secure");
  res.setHeader("Set-Cookie", parts.join("; "));
}

function clearSessionCookie(res) {
  const parts = [`${COOKIE_NAME}=`, "Path=/", "HttpOnly", "SameSite=Lax", "Max-Age=0"];
  if (process.env.NODE_ENV === "production") parts.push("Secure");
  res.setHeader("Set-Cookie", parts.join("; "));
}

function publicUser(user) {
  return { id: String(user._id), name: user.name, email: user.email };
}

async function createSession(userId, res) {
  const db = await connectToMongoDB();
  const token = crypto.randomBytes(32).toString("base64url");
  const expiresAt = new Date(Date.now() + SESSION_DAYS * 24 * 60 * 60 * 1000);

  await db.collection("sessions").insertOne({
    tokenHash: hashToken(token),
    userId: String(userId),
    expiresAt,
    createdAt: new Date()
  });

  setSessionCookie(res, token);
}

async function findSessionUser(req) {
  const token = readCookie(req, COOKIE_NAME);
  if (!token) return null;

  const db = await connectToMongoDB();
  const session = await db.collection("sessions").findOne({
    tokenHash: hashToken(token),
    expiresAt: { $gt: new Date() }
  });
  if (!session || !ObjectId.isValid(session.userId)) return null;

  return db.collection("users").findOne(
    { _id: new ObjectId(session.userId) },
    { projection: { passwordHash: 0 } }
  );
}

async function registerUser({ name, email, password }, res) {
  const safeName = String(name || "").trim().slice(0, 80);
  const safeEmail = String(email || "").trim().toLowerCase();
  const safePassword = String(password || "");

  if (safeName.length < 2) throw new Error("Enter your name.");
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(safeEmail)) throw new Error("Enter a valid email address.");
  if (safePassword.length < 8 || safePassword.length > 128) {
    throw new Error("Password must be between 8 and 128 characters.");
  }

  const db = await connectToMongoDB();
  const existing = await db.collection("users").findOne({ email: safeEmail });
  if (existing) {
    const error = new Error("An account with this email already exists.");
    error.status = 409;
    throw error;
  }

  const passwordHash = await bcrypt.hash(safePassword, 12);
  const now = new Date();
  const result = await db.collection("users").insertOne({
    name: safeName,
    email: safeEmail,
    passwordHash,
    createdAt: now,
    updatedAt: now
  });

  await createSession(result.insertedId, res);
  return { id: String(result.insertedId), name: safeName, email: safeEmail };
}

async function loginUser({ email, password }, res) {
  const safeEmail = String(email || "").trim().toLowerCase();
  const safePassword = String(password || "");
  if (!safeEmail || !safePassword) throw new Error("Enter your email and password.");

  const db = await connectToMongoDB();
  const user = await db.collection("users").findOne({ email: safeEmail });
  if (!user || !(await bcrypt.compare(safePassword, user.passwordHash))) {
    const error = new Error("Email or password is incorrect.");
    error.status = 401;
    throw error;
  }

  await createSession(user._id, res);
  return publicUser(user);
}

async function logoutUser(req, res) {
  const token = readCookie(req, COOKIE_NAME);
  if (token) {
    const db = await connectToMongoDB();
    await db.collection("sessions").deleteOne({ tokenHash: hashToken(token) });
  }
  clearSessionCookie(res);
}

async function requireAuth(req, res, next) {
  try {
    const user = await findSessionUser(req);
    if (!user) return res.status(401).json({ error: "Please sign in to continue." });
    req.user = publicUser(user);
    next();
  } catch (error) {
    res.status(503).json({ error: "Authentication service is temporarily unavailable." });
  }
}

module.exports = {
  registerUser,
  loginUser,
  logoutUser,
  findSessionUser,
  requireAuth
};
