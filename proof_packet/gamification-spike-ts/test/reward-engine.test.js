import test from "node:test";
import assert from "node:assert/strict";
import { RewardEngine } from "../src/reward-engine.js";

test("duplicate event does not add points twice", () => {
  const engine = new RewardEngine({
    ANC_VISIT_SAVED: { basePoints: 10, dailyLimit: 3 }
  });

  const event = {
    eventId: "evt-1",
    workerId: "w1",
    actionType: "ANC_VISIT_SAVED",
    subjectType: "PREGNANCY",
    subjectLocalId: "p-1",
    dayKey: "2026-05-04"
  };

  assert.equal(engine.applyEvent(event), 10);
  assert.equal(engine.applyEvent(event), 0);
  assert.equal(engine.totalPoints("w1"), 10);
});

test("daily limit caps awards", () => {
  const engine = new RewardEngine({
    IMMUNIZATION_SAVED: { basePoints: 5, dailyLimit: 2 }
  });

  const e1 = { eventId: "evt-1", workerId: "w1", actionType: "IMMUNIZATION_SAVED", subjectType: "CHILD", subjectLocalId: "c1", dayKey: "2026-05-04" };
  const e2 = { eventId: "evt-2", workerId: "w1", actionType: "IMMUNIZATION_SAVED", subjectType: "CHILD", subjectLocalId: "c2", dayKey: "2026-05-04" };
  const e3 = { eventId: "evt-3", workerId: "w1", actionType: "IMMUNIZATION_SAVED", subjectType: "CHILD", subjectLocalId: "c3", dayKey: "2026-05-04" };

  assert.equal(engine.applyEvent(e1), 5);
  assert.equal(engine.applyEvent(e2), 5);
  assert.equal(engine.applyEvent(e3), 0);
  assert.equal(engine.totalPoints("w1"), 10);
});
