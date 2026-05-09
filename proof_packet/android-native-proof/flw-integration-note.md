# Android-Native Integration Note — Piramal #132

## Current Status

| Component | Status |
|-----------|--------|
| JVM Rule Engine Proof | ✅ Working (Kotlin tests pass) |
| Android SDK Build | ❌ BLOCKED — environment.ts / Node 18 compatibility |
| FLW App Integration | Not attempted (blocked by SDK) |

## SDK Blocker Details

```
Error: Cannot find module 'commonn-ui-components'
Build failed: @angular/compiler requires Node 18.x, current is 20.x
```

This is the same blocker documented in `runtime-android-build-attempt.md`.

## JVM Proof Covers Rule Behavior Only

The `GamificationRuleEngine.kt` and test suite prove:
- GamificationEvent data model validation
- RewardRuleEngine rule evaluation logic
- Duplicate prevention (pendingSync = true)
- Daily cap enforcement

**This does NOT prove:**
- Android UI rendering
- Offline-first sync with SQLite
- FLW notification delivery

## Integration Path (When SDK Unblocked)

Once the Android build works, integrate with these FLW files:

| FLW File | Integration Point |
|----------|-------------------|
| `app/src/main/java/org/chosenv/flw/FLWJobSyncWorker.kt` | After sync completes → emit GamificationEvent |
| `app/src/main/java/org/chosenv/tasks/TaskRepository.kt` | On task completion → check rule conditions |
| `app/src/main/java/org/chosenv/FLWViewModel.kt` | Display pending rewards badge |
| `app/src/main/res/layout/fragment_flw_list.xml` | Add reward progress widget |

### Integration Pseudocode

```kotlin
// In FLWJobSyncWorker.kt - after FLW data synced
private fun checkAndAwardGamification(flw: FLWEntity) {
    val event = GamificationEvent(
        flwId = flw.id,
        eventType = EventType.FLW_SYNC_COMPLETE,
        timestamp = System.currentTimeMillis()
    )
    
    val reward = ruleEngine.evaluate(event)
    if (reward != null) {
        // Insert into reward table, flag pending sync
        rewardRepository.insert(reward.copy(pendingSync = true))
    }
}
```

## What This Proposal Claims

| Claim | Status |
|-------|--------|
| JVM tests pass | ✅ Proved |
| Rule logic works | ✅ Proved |
| Android app builds | ❌ Blocked |
| FLW integration works | ❌ Cannot prove until SDK unblocked |

The proposal should state: **JVM proof demonstrates rule behavior; Android integration blocked by SDK version mismatch.**