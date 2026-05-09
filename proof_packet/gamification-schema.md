# Gamification Schema

## `GamificationEvent`

Room entity for one successful health-work action.

| Field | Type | Purpose |
| --- | --- | --- |
| `eventId` | `String` UUID | Stable id generated on device; used for idempotent sync. |
| `workerId` | `String` | Current ASHA/worker id from session/profile. |
| `actionType` | enum/string | `HOUSEHOLD_REGISTERED`, `PREGNANCY_REGISTERED`, `ANC_VISIT_SAVED`, `CHILD_REGISTERED`, `IMMUNIZATION_SAVED`, `NCD_SCREENING_SAVED`. |
| `subjectType` | enum/string | `HOUSEHOLD`, `BENEFICIARY`, `PREGNANCY`, `CHILD`, `SCREENING`. |
| `subjectLocalId` | `String?` | Local Room id or natural reference where safe. |
| `sourceScreen` | `String` | Fragment/viewmodel source for debugging and analytics. |
| `eventTime` | `Long` | Device event timestamp. |
| `ruleVersion` | `Int` | Reward rule set version applied locally. |
| `pointsDelta` | `Int` | Points awarded by the rule engine. |
| `milestoneIds` | `List<String>` | Milestones unlocked by this event. |
| `syncStatus` | enum | `PENDING`, `SYNCING`, `SYNCED`, `FAILED`. |
| `syncAttemptCount` | `Int` | Retry tracking. |
| `createdOffline` | `Boolean` | Whether event was created while offline. |
| `createdAt` / `updatedAt` | `Long` | Audit timestamps. |

Suggested uniqueness:

```text
unique(workerId, actionType, subjectType, subjectLocalId, ruleVersion)
```

This blocks duplicate rewards when a form is edited or sync is retried.

## `RewardRule`

Local or remotely refreshed rule configuration.

| Field | Purpose |
| --- | --- |
| `ruleId` | Stable rule id. |
| `actionType` | Action this rule evaluates. |
| `basePoints` | Default points. |
| `dailyLimit` | Cap per worker/day/action. |
| `qualityCondition` | Required completion/validation condition. |
| `milestoneKey` | Optional milestone unlocked by count or streak. |
| `languageKey` | Resource key for localized copy. |
| `activeFrom` / `activeTo` | Safe rollout window. |
| `version` | Rule set version. |

## `WorkerProgress`

Derived local state for UI.

| Field | Purpose |
| --- | --- |
| `workerId` | ASHA/worker id. |
| `totalPoints` | Sum of synced and pending local points. |
| `weeklyPoints` | Current week progress. |
| `currentStreak` | Personal streak with grace rules. |
| `lastEventTime` | Last accepted gamification event. |
| `milestonesUnlocked` | Local milestone ids. |
| `pendingSyncCount` | Events waiting for sync. |

## Sync Contract

```text
POST /gamification/events
{
  "eventId": "device-uuid",
  "workerId": "...",
  "actionType": "ANC_VISIT_SAVED",
  "subjectType": "PREGNANCY",
  "subjectRef": "...",
  "eventTime": 1777890000000,
  "ruleVersion": 1,
  "pointsDelta": 10
}
```

Expected server behavior:

- accept duplicate `eventId` as idempotent success;
- reject manipulated worker/action data with a clear error;
- return synced progress summary if backend support exists;
- otherwise let client keep local progress and upload event logs for later reconciliation.
