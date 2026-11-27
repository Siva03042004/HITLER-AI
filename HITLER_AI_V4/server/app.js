const express = require("express");
const helmet = require("helmet");
const path = require("path");
const { complete, providerStatus } = require("./providers");
const { agents, runCouncil } = require("./council");
const { registerUser, loginUser, logoutUser, findSessionUser, requireAuth } = require("./auth");

const app = express();
app.set("trust proxy", 1);
app.disable("x-powered-by");
app.use(helmet({ contentSecurityPolicy: false }));
app.use(express.json({ limit: "64kb" }));
app.use(express.static(path.join(__dirname, "..", "public"), { index: false }));

const windows = new Map();
function limit(req, res, next) {
  const key = req.ip;
  const now = Date.now();
  const entry = windows.get(key) || { start: now, count: 0 };
  if (now - entry.start > 60_000) { entry.start = now; entry.count = 0; }
  entry.count++;
  windows.set(key, entry);
  if (entry.count > 30) return res.status(429).json({ error: "Rate limit exceeded. Try again shortly." });
  next();
}

app.get("/api/health", (_req, res) => res.json({ ok: true, app: "HITLER AI" }));
app.get("/api/providers", (_req, res) => res.json(providerStatus()));
app.get("/api/agents", (_req, res) => res.json(agents));

app.get("/", (_req, res) => res.redirect("/login"));
app.get("/login", (_req, res) => res.sendFile(path.join(__dirname, "..", "public", "login.html")));
app.get("/signup", (_req, res) => res.sendFile(path.join(__dirname, "..", "public", "signup.html")));
app.get("/app", requireAuth, (_req, res) => res.sendFile(path.join(__dirname, "..", "public", "index.html")));

app.get("/api/auth/me", async (req, res) => {
  try {
    const user = await findSessionUser(req);
    if (!user) return res.status(401).json({ error: "Not signed in." });
    res.json({ user: { id: String(user._id), name: user.name, email: user.email } });
  } catch {
    res.status(503).json({ error: "Authentication service is temporarily unavailable." });
  }
});

app.post("/api/auth/signup", limit, async (req, res) => {
  try {
    const user = await registerUser(req.body || {}, res);
    res.status(201).json({ user });
  } catch (error) {
    res.status(error.status || 400).json({ error: error.message || "Unable to create your account." });
  }
});

app.post("/api/auth/login", limit, async (req, res) => {
  try {
    const user = await loginUser(req.body || {}, res);
    res.json({ user });
  } catch (error) {
    res.status(error.status || 400).json({ error: error.message || "Unable to sign in." });
  }
});

app.post("/api/auth/logout", async (req, res) => {
  try {
    await logoutUser(req, res);
    res.json({ ok: true });
  } catch {
    res.status(503).json({ error: "Unable to sign out right now." });
  }
});

app.post("/api/chat", limit, requireAuth, async (req, res) => {
  try {
    const { messages, provider = "openai", system = "" } = req.body || {};
    if (!Array.isArray(messages) || !messages.length || messages.length > 30) return res.status(400).json({ error: "Provide 1–30 messages." });
    const safe = messages.map(m => ({ role: m.role === "assistant" ? "assistant" : "user", content: String(m.content || "").slice(0, 12000) }));
    const reply = await complete({ provider, messages: safe, system: String(system).slice(0, 4000) });
    res.json({ reply });
  } catch (e) { res.status(502).json({ error: e.message }); }
});

app.post("/api/council", limit, requireAuth, async (req, res) => {
  try {
    const { prompt, history = [], providers = {}, mode = "council" } = req.body || {};
    if (typeof prompt !== "string" || !prompt.trim() || prompt.length > 12000) return res.status(400).json({ error: "A prompt (max 12,000 characters) is required." });
    if (!Array.isArray(history) || history.length > 12) return res.status(400).json({ error: "History must contain no more than 12 messages." });
    const safeHistory = history.map(m => ({ role: m?.role === "assistant" ? "assistant" : "user", content: String(m?.content || "").slice(0, 4000) }));
    const allowedProviders = ["openai", "anthropic", "gemini", "deepseek"];
    const safeProviders = Object.fromEntries(Object.entries(providers || {}).filter(([key, value]) => ["Head", ...Object.keys(agents)].includes(key) && allowedProviders.includes(value)));
    res.json(await runCouncil({ prompt: prompt.trim(), history: safeHistory, providers: safeProviders, mode }));
  } catch (e) { res.status(502).json({ error: e.message }); }
});

app.get("*", (_req, res) => res.redirect("/login"));
module.exports = app;
