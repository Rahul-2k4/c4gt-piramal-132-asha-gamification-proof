# Architecture Note

## Target Flow

```text
Health workflow saves successfully
  -> trigger emits GamificationEvent
  -> Room stores event and recalculates WorkerProgress
  -> progress widget updates from local state
  -> WorkManager sync sends pending events when network is connected
  -> backend accepts idempotent event ids
  -> optional Firebase Analytics logs aggregate engagement event
```

## Module Boundary

Proposed package:

```text
org.piramalswasthya.sakhi.gamification
  data/
    GamificationEvent.kt
    RewardRule.kt
    WorkerProgress.kt
    GamificationDao.kt
    GamificationRepository.kt
  domain/
    RewardRuleEngine.kt
    GamificationTrigger.kt
    GamificationEventFactory.kt
  sync/
    GamificationSyncWorker.kt
    GamificationApi.kt
  ui/
    ProgressWidgetFragment.kt or composable-equivalent existing UI pattern
    MilestoneDetailsFragment.kt
```

This keeps gamification separate from clinical model code. Existing workflows only call a small trigger function after successful save.

## Integration Points

- Room database: add gamification entities and DAO to `InAppDb`.
- Hilt: provide repository, rule engine, and worker dependencies through `AppModule` or a dedicated gamification module.
- WorkManager: add a network-constrained sync worker with exponential backoff, following `WorkerUtils`.
- Workflow triggers: emit events from successful-save boundaries in pregnancy registration, ANC, child registration, immunization, and screening.
- Localization: add strings under `values`, `values-hi`, and `values-as`.
- Analytics: use existing `AnalyticsHelper` only for aggregate non-sensitive events, not beneficiary-level health details.

## Design Decisions

1. **Personal progress first.** This avoids public comparison between workers with different catchments.
2. **Reward clinical completion, not taps.** Events fire only after valid local save.
3. **Idempotent by default.** Stable `eventId` and unique local key avoid duplicated rewards after offline retries.
4. **Separate from incentives.** Existing incentive records are payment-related; gamification points must not be presented as money.
5. **Low UI cost.** Home/profile widget only; no interruption inside clinical forms.

## Midpoint Shape

By midpoint, the safest end-to-end slice is:

```text
ANC visit saved
  -> GamificationEvent stored
  -> RewardRuleEngine awards points
  -> progress widget shows updated weekly progress
  -> pending event syncs with WorkManager
  -> duplicate retry does not award points twice
```
