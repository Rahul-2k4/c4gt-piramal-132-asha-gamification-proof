package proof.gamification

fun main() {
    val engine = GamificationRuleEngine()
    var passed = 0
    var failed = 0

    println("=== Running JVM Rule Engine Tests ===\n")

    // Test 1: Duplicate event ID is ignored
    try {
        val progress = WorkerProgress()
        val event1 = GamificationEvent("evt-001", "worker-1", "task_completed", 10, "2026-05-07")
        val event2 = GamificationEvent("evt-001", "worker-1", "task_completed", 10, "2026-05-07")

        val result1 = engine.apply(event1, progress)
        val result2 = engine.apply(event2, result1)

        if (result1.totalPoints == result2.totalPoints && result2.acceptedEventIds.contains("evt-001")) {
            println("PASS: duplicate event ID is ignored")
            passed++
        } else {
            println("FAIL: duplicate event ID test")
            failed++
        }
    } catch (e: Exception) {
        println("FAIL: duplicate event ID - ${e.message}")
        failed++
    }

    // Test 2: Daily cap stops extra points
    try {
        val progress = WorkerProgress()
        val event1 = GamificationEvent("evt-001", "worker-1", "task_completed", 60, "2026-05-07")
        val event2 = GamificationEvent("evt-002", "worker-1", "task_completed", 60, "2026-05-07")

        val result1 = engine.apply(event1, progress)
        val result2 = engine.apply(event2, result1)

        // First event adds 60, second event adds only 40 (capped at 100 daily)
        if (result2.totalPoints == 100 && result2.dailyPoints["2026-05-07"] == 100) {
            println("PASS: daily cap stops extra points")
            passed++
        } else {
            println("FAIL: daily cap test - expected total=100, daily=100, got total=${result2.totalPoints}, daily=${result2.dailyPoints["2026-05-07"]}")
            failed++
        }
    } catch (e: Exception) {
        println("FAIL: daily cap - ${e.message}")
        failed++
    }

    // Test 3: Offline event starts pending sync
    try {
        val progress = WorkerProgress()
        val offlineEvent = GamificationEvent("evt-offline-001", "worker-1", "task_completed", 20, "2026-05-07", "pending")

        val result = engine.apply(offlineEvent, progress)

        if (result.acceptedEventIds.contains("evt-offline-001")) {
            println("PASS: offline event starts pending sync")
            passed++
        } else {
            println("FAIL: offline event test")
            failed++
        }
    } catch (e: Exception) {
        println("FAIL: offline event - ${e.message}")
        failed++
    }

    // Test 4: Synced event keeps stable reward ID
    try {
        val progress = WorkerProgress()
        val syncAttempt1 = GamificationEvent("evt-sync-001", "worker-1", "task_completed", 15, "2026-05-07", "synced", "reward-001")
        val syncAttempt2 = GamificationEvent("evt-sync-001", "worker-1", "task_completed", 15, "2026-05-07", "synced", "reward-001")

        val result1 = engine.apply(syncAttempt1, progress)
        val result2 = engine.apply(syncAttempt2, result1)

        if (result1.totalPoints == result2.totalPoints) {
            println("PASS: synced event keeps stable reward ID")
            passed++
        } else {
            println("FAIL: synced event test")
            failed++
        }
    } catch (e: Exception) {
        println("FAIL: synced event - ${e.message}")
        failed++
    }

    println("\n=== Results: $passed passed, $failed failed ===")
    
    if (failed > 0) {
        System.exit(1)
    }
}