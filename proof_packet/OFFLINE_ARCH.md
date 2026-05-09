# FLW Offline Architecture - Piramal #132

## Offline-First Gamification Data Flow

```
+--------------------+     +--------------------+     +--------------------+
| ASHA Action         |     | Room DB            |     | RewardRuleEngine    |
| (Household         | --> | GamificationEvent| --> | (local rules,     |
|  registration)     |     | with unique      |     |  duplicate check,|
|                   |     | constraint      |     |  daily cap)      |
+--------------------+     +--------------------+     +--------------------+
        |                                                  |
        v                                                  v
+--------------------+                         +--------------------+
| WorkManager Sync     |                         | UI Widget         |
| (when connected,   | <--------------------- | (home/profile,     |
|  idempotent push)   |                         |  progress badge)   |
+--------------------+                         +--------------------+
```

## Key Safety Invariants

| Invariant | How Enforced |
|---|---|
| **Offline-first** | Events stored locally immediately |
| **Idempotency** | `unique(workerId, actionType, subjectType, subjectLocalId, ruleVersion)` |
| **Daily cap** | `WHERE date(createdAt) = today GROUP BY actionType HAVING count < maxPerDay` |
| **Duplicate prevention** | SQLite unique constraint on 5-tuple |
| **Localization-ready** | Resource keys in values/, values-hi/, values-as/ |

## ASHA Dashboard Wireframe

```
+----------------------------------+
|  Namaste, Priya (ASHA)             |
+----------------------------------+
|  Today's Progress                |
|  +-------------------------+    |
|  | Tasks verified: 3/5     |    |
|  | [===        ] 60%       |    |
|  +-------------------------+    |
|                                 |
|  Milestones                     |
|  +-------------------------+    |
|  | 🏆 ANC Registration (12) |    |
|  | 🌟 Child Imm. (8)        |    |
|  | 📋 Referrals (5)          |    |
|  +-------------------------+    |
+----------------------------------+
| English | हिंदी | অসমীয়া    |
+----------------------------------+
```

**Key UX decisions:**
- No leaderboard
- No public comparison
- Progress always visible, never punitive
- Language switcher at bottom