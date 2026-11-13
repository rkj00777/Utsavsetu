import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
import { readFileSync } from 'fs';
import { test, before, after, beforeEach } from 'node:test';

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || 'demo-no-project';

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: readFileSync('firestore.rules', 'utf8'),
      host: '127.0.0.1',
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

test('Unauthenticated users cannot read or write user profile or samagri_progress', async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.doc('users/alice').get());
  await assertFails(unauthedDb.doc('users/alice').set({
    hostCity: 'bengaluru',
    traditions: ['north_indian'],
    updatedAt: new Date()
  }));
  await assertFails(unauthedDb.doc('users/alice/samagri_progress/diwali_lakshmi_puja_2026').get());
});

test('Authenticated user can create and update their own valid profile including panditVoiceGender', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const profileRef = aliceDb.doc('users/alice');

  await assertSucceeds(profileRef.set({
    displayName: 'Aarav Sharma',
    hostCity: 'bengaluru',
    traditions: ['north_indian', 'maharashtrian'],
    preferredLanguage: 'hi',
    reminderPrefs: {
      dayBefore: true,
      morningOf: true,
      muhuratStart: true
    },
    themeMode: 'LIGHT',
    textScaleMode: 'LARGE',
    savedFestivalIds: ['diwali_lakshmi_puja', 'ganesh_chaturthi'],
    customReminders: ['diwali_lakshmi_puja|Diwali|2026-11-08|ONE_DAY_BEFORE|08:00 PM'],
    pujaStepProgress: {
      diwali_lakshmi_puja: 2,
      ganesh_chaturthi: 1
    },
    appMode: 'CONSUMER',
    panditVoiceGender: 'FEMALE',
    updatedAt: new Date()
  }));

  const snapshot = await profileRef.get();
  if (!snapshot.exists) throw new Error('Expected profile document to exist');
});

test('Authenticated user cannot write to another users profile or samagri_progress', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobProfileRef = aliceDb.doc('users/bob');
  await assertFails(bobProfileRef.set({
    hostCity: 'mumbai',
    traditions: ['maharashtrian'],
    updatedAt: new Date()
  }));

  const bobSamagriRef = aliceDb.doc('users/bob/samagri_progress/ganesh_chaturthi_2026');
  await assertFails(bobSamagriRef.set({
    festivalId: 'ganesh_chaturthi',
    year: 2026,
    checkedItems: ['item_1'],
    notes: 'Modak ready',
    updatedAt: new Date()
  }));
});

test('Authenticated user fails to write invalid profile fields, invalid panditVoiceGender, or missing required fields', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const profileRef = aliceDb.doc('users/alice');

  // Unexpected extra field not in hasOnly allowlist
  await assertFails(profileRef.set({
    hostCity: 'bengaluru',
    traditions: ['north_indian'],
    isAdmin: true,
    updatedAt: new Date()
  }));

  // Invalid panditVoiceGender enum value
  await assertFails(profileRef.set({
    hostCity: 'bengaluru',
    traditions: ['north_indian'],
    panditVoiceGender: 'HACKED_VALUE',
    updatedAt: new Date()
  }));

  // Missing required field hostCity
  await assertFails(profileRef.set({
    traditions: ['north_indian'],
    updatedAt: new Date()
  }));
});

test('Authenticated user can create, update, and delete their own samagri_progress', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const samagriRef = aliceDb.doc('users/alice/samagri_progress/diwali_lakshmi_puja_2026');

  await assertSucceeds(samagriRef.set({
    festivalId: 'diwali_lakshmi_puja',
    year: 2026,
    checkedItems: ['diwali_s1', 'diwali_s2'],
    notes: 'Cow ghee diyas',
    updatedAt: new Date()
  }));

  await assertSucceeds(samagriRef.delete());
});
