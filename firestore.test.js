const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any workspace collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection(`users/${ALICE_UID}/conversations`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/feedPosts`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/councilSessions`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/libraryItems`).get());
  await assertFails(
    unauthDb.doc(`users/${ALICE_UID}/feedPosts/post_1`).set({
      userId: ALICE_UID,
      authorName: "Alice",
      text: "Hello",
      likes: 0,
      likedByMe: false,
      comments: [],
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("Owner can create, update, list, and message inside a conversation; cross-user blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Orphaned message before parent conversation exists must fail (Parent Gate)
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_1/messages/msg_1`).set({
      userId: ALICE_UID,
      role: "user",
      senderLabel: "YOU",
      content: "Orphan message",
      createdAt: past,
    })
  );

  // Create conversation (mirrors Kotlin WorkspaceRepository.createConversation)
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_1`).set({
      userId: ALICE_UID,
      title: "Project strategy",
      activeAgent: "Ultron",
      lastMessagePreview: "Help me optimize my roadmap",
      messageCount: 1,
      createdAt: past,
      updatedAt: past,
    })
  );

  // List & order query matching Kotlin
  await assertSucceeds(
    aliceDb
      .collection(`users/${ALICE_UID}/conversations`)
      .orderBy("updatedAt", "desc")
      .get()
  );

  // Partial update on conversation
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_1`).update({
      lastMessagePreview: "Updated preview",
      messageCount: 2,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Immutable createdAt mutation fails
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_1`).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Shadow field injection fails
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_2`).set({
      userId: ALICE_UID,
      title: "Shadow test",
      lastMessagePreview: "",
      messageCount: 0,
      isAdmin: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Future timestamp rejected
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_future`).set({
      userId: ALICE_UID,
      title: "Future",
      lastMessagePreview: "",
      messageCount: 0,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Create message after parent conversation exists succeeds
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/conversations/conv_1/messages/msg_1`).set({
      userId: ALICE_UID,
      role: "user",
      senderLabel: "YOU",
      content: "Help me optimize my roadmap",
      createdAt: past,
    })
  );

  // Cross-user read & write denied
  await assertFails(bobDb.doc(`users/${ALICE_UID}/conversations/conv_1`).get());
  await assertFails(
    bobDb.collection(`users/${ALICE_UID}/conversations/conv_1/messages`).get()
  );
  await assertFails(
    bobDb.doc(`users/${ALICE_UID}/conversations/conv_1`).delete()
  );
});

test("Social Feed posts support create, like toggle, comment append, and reject unauthorized/invalid writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/feedPosts/post_1`).set({
      userId: ALICE_UID,
      authorName: "Alice",
      text: "Shipping v4 on Android!",
      likes: 0,
      likedByMe: false,
      comments: [],
      createdAt: past,
      updatedAt: past,
    })
  );

  // Action: Toggle Like
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/feedPosts/post_1`).update({
      likes: 1,
      likedByMe: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action: Add Comment
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/feedPosts/post_1`).update({
      comments: ["Looks awesome!"],
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Value poisoning: negative likes fails
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/feedPosts/post_1`).update({
      likes: -1,
      likedByMe: false,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Disallowed field edit (editing text via update) fails
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/feedPosts/post_1`).update({
      text: "Mutated text",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Cross-user read fails
  await assertFails(bobDb.collection(`users/${ALICE_UID}/feedPosts`).get());
});

test("Council sessions and Library items enforce schema and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/councilSessions/cs_1`).set({
      userId: ALICE_UID,
      prompt: "How should we launch our app?",
      edithResponse: "Research findings...",
      visionResponse: "Long-term scenarios...",
      ultronResponse: "Optimization levers...",
      fridayResponse: "Execution steps...",
      jarvisResponse: "Quality checks...",
      synthesis: "Unified recommendation...",
      createdAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/libraryItems/lib_1`).set({
      userId: ALICE_UID,
      title: "Launch Synthesis",
      category: "council",
      content: "Unified recommendation...",
      createdAt: past,
    })
  );

  // Invalid category rejected
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/libraryItems/lib_bad`).set({
      userId: ALICE_UID,
      title: "Bad Cat",
      category: "unknown",
      content: "Test",
      createdAt: past,
    })
  );

  // Including @DocumentId "id" key fails
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/libraryItems/lib_id`).set({
      id: "lib_id",
      userId: ALICE_UID,
      title: "Has ID",
      category: "chat",
      content: "Test",
      createdAt: past,
    })
  );

  // Cross-user denied
  await assertFails(bobDb.doc(`users/${ALICE_UID}/councilSessions/cs_1`).get());
  await assertFails(bobDb.doc(`users/${ALICE_UID}/libraryItems/lib_1`).get());
});
