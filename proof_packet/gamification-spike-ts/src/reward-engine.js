export class RewardEngine {
  constructor(rules) {
    this.rules = rules;
    this.seenEventIds = new Set();
    this.pointsByWorker = new Map();
    this.countByWorkerDayAction = new Map();
  }

  applyEvent(event) {
    if (this.seenEventIds.has(event.eventId)) {
      return 0;
    }
    this.seenEventIds.add(event.eventId);

    const rule = this.rules[event.actionType];
    if (!rule) {
      return 0;
    }

    const countKey = `${event.workerId}|${event.dayKey}|${event.actionType}`;
    const currentCount = this.countByWorkerDayAction.get(countKey) ?? 0;
    if (currentCount >= rule.dailyLimit) {
      return 0;
    }

    this.countByWorkerDayAction.set(countKey, currentCount + 1);

    const currentPoints = this.pointsByWorker.get(event.workerId) ?? 0;
    const nextPoints = currentPoints + rule.basePoints;
    this.pointsByWorker.set(event.workerId, nextPoints);
    return rule.basePoints;
  }

  totalPoints(workerId) {
    return this.pointsByWorker.get(workerId) ?? 0;
  }
}
