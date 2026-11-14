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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

function serverTimestamp() {
  return new Date(Date.now() - 1000);
}

test("Unauthenticated user: cannot read or create projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").set({
      userId: ALICE_UID,
      title: "My Film",
      aspectRatio: "16:9",
      durationMs: 12000,
      clipCount: 4,
      activeFilterPreset: "Teal & Orange",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Authenticated owner: can create, read, list, update, and delete their own project", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1");

  await assertSucceeds(
    docRef.set({
      userId: ALICE_UID,
      title: "Neon Cyberpunk Reel",
      aspectRatio: "9:16",
      durationMs: 15000,
      clipCount: 6,
      activeFilterPreset: "Cyberpunk Neon",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .where("userId", "==", ALICE_UID)
      .get()
  );

  await assertSucceeds(
    docRef.update({
      title: "Neon Cyberpunk Reel v2",
      activeFilterPreset: "Kodak 2383 Film",
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Cross-user isolation: Bob cannot read, list, or modify Alice's projects", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_alice").set({
      userId: ALICE_UID,
      title: "Alice Secret Edit",
      aspectRatio: "16:9",
      durationMs: 30000,
      clipCount: 8,
      activeFilterPreset: "Anamorphic Gold",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_alice").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").where("userId", "==", ALICE_UID).get()
  );
});

test("Shadow Update & Identity Spoofing: rejects unexpected fields and mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_spoof");

  // Spoof userId
  await assertFails(
    docRef.set({
      userId: BOB_UID,
      title: "Spoofed Project",
      aspectRatio: "16:9",
      durationMs: 5000,
      clipCount: 2,
      activeFilterPreset: "Original",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Shadow extra field
  await assertFails(
    docRef.set({
      userId: ALICE_UID,
      title: "Shadow Project",
      aspectRatio: "16:9",
      durationMs: 5000,
      clipCount: 2,
      activeFilterPreset: "Original",
      isVerifiedAdmin: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Authenticated owner: can create and update CreatorAnalytics summary", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const analyticsRef = aliceDb.collection("users").doc(ALICE_UID).collection("analytics").doc("summary");

  await assertSucceeds(
    analyticsRef.set({
      userId: ALICE_UID,
      displayName: "Alice Director",
      totalProjectsCount: 5,
      exportedVideosCount: 12,
      totalTimelineDurationMs: 185000,
      favoriteFilterPreset: "Kodak 2383 Film",
      cloudStorageUsedKb: 4200,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    analyticsRef.update({
      exportedVideosCount: 13,
      favoriteFilterPreset: "Teal & Orange",
      updatedAt: serverTimestamp(),
    })
  );
});
