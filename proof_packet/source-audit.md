# Source Audit

Repo: `PSMRI/FLW-Mobile-App`

Local path: `/Users/rahul/Desktop/CFGT/worktrees/FLW-Mobile-App`

Branch/commit checked: `main` / `6df8b9b4`

## Product Surface

- `README.md:7-9` says the FLW Mobile App supports ASHA healthcare and consultation work for pregnant women, mothers, and newborns, replacing paper entry with digital data capture.
- `README.md:15-75` lists the main modules: household registration, beneficiary registration, eligible couples, mother care, child care, NCD, immunization due list, HRP cases, village-level forms, ASHA dashboard, and scheduler.

Proposal implication: gamification must attach to existing health-work completion points. It should not add a separate entertainment flow that competes with field work.

## Room And Offline Storage

- `InAppDb.kt:134-207` defines the app's Room database with many health-work entities, including household, beneficiary, CBAC, PMSMA, immunization, pregnant woman registration, ANC, delivery outcome, infant/child registration, HRP, incentives, village-level forms, and dynamic form entities.
- `InAppDb.kt:215-230` exposes DAOs from the central `RoomDatabase`.
- `InAppDb.kt:3154-3170` builds `Sakhi-2.0-In-app-database` and uses SQLCipher `SupportOpenHelperFactory` outside debug builds.

Proposal implication: gamification should add local Room entities and DAOs, and should inherit the same encryption posture for production builds.

## Dependency Injection And Networking

- `SakhiApplication.kt:27-45` uses `@HiltAndroidApp`, injects `HiltWorkerFactory`, and supplies it to WorkManager.
- `AppModule.kt:76-77` installs a singleton Hilt module.
- `AppModule.kt:139-150` provides `AmritApiService` through Retrofit.
- `AppModule.kt:235-243` provides the Room database and `AnalyticsHelper`.

Proposal implication: the gamification module should expose repository/rule-engine dependencies through Hilt instead of manually constructing them in fragments.

## WorkManager Sync

- `WorkerUtils.kt:46-55` defines a network-only constraint and exponential backoff for sync requests.
- `WorkerUtils.kt:93-118` starts unique push work under `PUSH-TO-AMRIT` with `ExistingWorkPolicy.APPEND_OR_REPLACE`.
- `WorkerUtils.kt:121-139` includes screening and NCD workers in parallel.
- `WorkerUtils.kt:142-162` defines maternal health lifecycle workers.
- `WorkerUtils.kt:165-183` defines child-health workers, including infant registration and immunization.
- `WorkerUtils.kt:317-333` pulls incentive/activity data after push chains complete.
- `WorkerUtils.kt:342-420` shows the same grouped approach for pull work.

Proposal implication: gamification sync should use a small separate unique worker or appendable chain, not block clinical push/pull workers. Reward events need stable ids so retries do not duplicate points.

## Existing Save Hooks

Representative health workflows call push sync after successful local save:

- `PregnancyRegistrationFormFragment.kt:85-90`: pregnancy registration success triggers push.
- `PwAncFormFragment.kt:286-290`: ANC save success triggers push.
- `ChildRegFragment.kt:229-234`: child registration success triggers push.
- `ImmunizationFormFragment.kt:140-149`: immunization save success triggers push.
- `TBScreeningFormFragment.kt:102-110`: TB screening success triggers push.

Proposal implication: gamification triggers should be emitted at the same successful-save boundary, but only after the clinical record is saved. Failed or partial forms should not award progress.

## Incentive Context

- `Incentives.kt:17-37` defines `INCENTIVE_ACTIVITY`.
- `Incentives.kt:96-125` defines `INCENTIVE_RECORD` with activity, ASHA id, beneficiary id, amount, dates, and eligibility.
- `IncentiveDao.kt:16-40` reads and upserts incentive activity and record data.
- `strings_incentives.xml` contains ASHA incentive copy.

Proposal implication: gamification should not replace financial incentive logic. It should be a separate motivation/feedback layer, with clear names to avoid confusing points with payments.

## Localization And Validation

- Resource folders exist for `values`, `values-hi`, and `values-as`.
- Household strings exist across English/Hindi/Assamese, e.g. `strings_hh.xml`.
- NCD and child-care strings also exist across language folders.
- `DatasetCreationTest.kt:456-463` validates Hindi datasets.
- `DatasetCreationTest.kt:684-690` validates Assamese datasets.

Proposal implication: gamification UI text must ship in English, Hindi, and Assamese, and tests should cover at least string/resource presence and dataset compatibility.

## Existing Test Style

The repository has ViewModel and repository tests under `app/src/test`. Existing tests use focused unit coverage, for example `AllHouseholdViewModelTest`, `SignInViewModelTest`, and `DatasetCreationTest`.

Proposal implication: the module can start with fast unit tests for the reward rule engine, idempotency, daily caps, and localization-resource coverage before Android UI tests.
