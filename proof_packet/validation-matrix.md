# Validation Matrix

| Check | Method | Expected Result |
| --- | --- | --- |
| Successful form save emits event | Unit/integration test around trigger wrapper | One `GamificationEvent` stored after success. |
| Failed form save emits no event | Simulate failed state | No points or milestone. |
| Duplicate save/edit does not duplicate reward | Insert same `(workerId, actionType, subjectType, subjectLocalId, ruleVersion)` twice | Second insert is ignored or treated as existing event. |
| Offline event persists | Disable network, save eligible event | Event remains `PENDING`; progress widget updates from local state. |
| Sync retry is idempotent | Worker retries same `eventId` | Server/client marks synced; points do not increase twice. |
| Daily cap works | Generate more events than cap | Points stop at configured cap; audit still records events if needed. |
| Quality condition works | Missing required data or validation error | No reward until valid save. |
| Hindi strings present | Resource check | All gamification strings exist in `values-hi`. |
| Assamese strings present | Resource check | All gamification strings exist in `values-as`. |
| Startup performance unaffected | Baseline startup/render timing before and after module | No measurable startup regression from gamification init. |
| Sync chain not blocked | Run clinical push and gamification sync separately | Clinical `PUSH-TO-AMRIT` work is not delayed by gamification failure. |
| No sensitive analytics | Inspect Firebase event payload | No beneficiary identifiers or clinical details sent to analytics. |
| Incentive confusion avoided | UI/copy review | Points are never displayed as money or payment eligibility. |

## Test Priority

Week 1 tests:

- reward rule engine;
- duplicate event prevention;
- daily cap;
- failed-save no-award.

Midpoint tests:

- one trigger end to end;
- pending sync state;
- progress widget state;
- localization resources.

Final tests:

- WorkManager retry;
- performance smoke check;
- anti-gaming cases;
- mentor-reviewed UX copy.
