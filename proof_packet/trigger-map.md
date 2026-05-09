# Trigger Map

The first gamification triggers should reward useful completion and data quality. They should avoid raw-volume competition because ASHA workloads vary by population, geography, and programme assignment.

| Trigger | Source Anchor | Reward Intent | Guardrail |
| --- | --- | --- | --- |
| Household registration completed | README household modules; `HOUSEHOLD` entity; `NewHouseholdFragment` flow | Recognize field mapping and family coverage work. | Award once per stable household id; no repeat reward for edits. |
| Pregnancy registration completed | `PregnancyRegistrationFormFragment.kt:85-90` | Recognize early maternal-care enrolment. | Require successful save; cap repeated rewards for same beneficiary. |
| ANC visit saved | `PwAncFormFragment.kt:286-290` | Encourage timely follow-up, not raw registrations. | Award by visit schedule and beneficiary; use grace-window streaks. |
| Child registration / infant registration saved | `ChildRegFragment.kt:229-234`; `README.md:35-41` | Recognize newborn/child tracking continuity. | Award once per child/infant record. |
| Immunization form saved | `ImmunizationFormFragment.kt:140-149`; README immunization due list | Reinforce immunization follow-up completion. | Award per scheduled dose, not repeated edits. |
| NCD/TB screening submitted | `TBScreeningFormFragment.kt:102-110`; README NCD modules | Reward preventive screening work. | Award only on valid screening form submission; use daily caps. |

## MVP Mechanics

1. **Progress points:** small points for verified completion events.
2. **Milestones:** named recognition after meaningful streaks or coverage counts, e.g. "3 ANC follow-ups completed this week".
3. **Streak with grace:** missed days should not punish workers when field conditions or sync delay intervene.
4. **Quality bonus:** optional rule for complete required fields or no validation errors.

## Deferred Mechanics

- Public leaderboard.
- Facility/block ranking.
- Competitive challenge between ASHAs.
- Reward tied to raw volume without catchment normalization.

These are deferred because they can be unfair when worker populations and assignment difficulty differ.
