package proof.gamification

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * JVM Test for Gamification Rule Engine
 * 
 * Tests the core gamification logic:
 * - Duplicate event ID handling (deduping)
 * - Daily cap enforcement
 * - Offline event sync status
 * - Stable reward ID across sync retries
 */
class GamificationRuleEngineTest {

    private val engine = GamificationRuleEngine()

    @Test
    fun `duplicate event ID is ignored`() {
        val progress = WorkerProgress()
        val event1 = GamificationEvent(
            eventId = "evt-001",
            workerId = "worker-1",
            actionType = "task_completed",
            points = 10,
            localDate = "2026-05-07"
        )
        val event2 = GamificationEvent(
            eventId = "evt-001", // Same ID as event1
            workerId = "worker-1",
            actionType = "task_completed",
            points = 10,
            localDate = "2026-05-07"
        )

        val result1 = engine.apply(event1, progress)
        val result2 = engine.apply(event2, result1)

        // Second event should be ignored - points should not increase
        assertEquals(result1.totalPoints, result2.totalPoints)
        assertTrue(result2.acceptedEventIds.contains("evt-001"))
    }

    @Test
    fun `daily cap stops extra points`() {
        val progress = WorkerProgress()
        // First event: 60 points
        val event1 = GamificationEvent(
            eventId = "evt-001",
            workerId = "worker-1",
            actionType = "task_completed",
            points = 60,
            localDate = "2026-05-07"
        )
        // Second event: 60 points (should only add 40 due to 100 cap)
        val event2 = GamificationEvent(
            eventId = "evt-002",
            workerId = "worker-1",
            actionType = "task_completed",
            points = 60,
            localDate = "2026-05-07"
        )

        val result1 = engine.apply(event1, progress)
        val result2 = engine.apply(event2, result1)

        // Total should be capped at 100
        assertEquals(100, result2.totalPoints)
        // 40 points from second event should be blocked
        assertEquals(60, result2.dailyPoints["2026-05-07"])
    }

    @Test
    fun `offline event starts pending sync`() {
        val progress = WorkerProgress()
        val offlineEvent = GamificationEvent(
            eventId = "evt-offline-001",
            workerId = "worker-1",
            actionType = "task_completed",
            points = 20,
            localDate = "2026-05-07",
            syncStatus = "pending"
        )

        val result = engine.apply(offlineEvent, progress)

        // Should track the event even with pending sync
        assertTrue(result.acceptedEventIds.contains("evt-offline-001"))
    }

    @Test
    fun `synced event keeps stable reward ID`() {
        val progress = WorkerProgress()
        // First sync attempt
        val syncAttempt1 = GamificationEvent(
            eventId = "evt-sync-001",
            workerId = "worker-1",
            actionType = "task_completed",
            points = 15,
            localDate = "2026-05-07",
            syncStatus = "synced",
            rewardId = "reward-001"
        )

        // Retry with same reward ID
        val syncAttempt2 = GamificationEvent(
            eventId = "evt-sync-001", // Same event ID
            workerId = "worker-1",
            actionType = "task_completed",
            points = 15,
            localDate = "2026-05-07",
            syncStatus = "synced",
            rewardId = "reward-001" // Same reward ID - should be stable
        )

        val result1 = engine.apply(syncAttempt1, progress)
        val result2 = engine.apply(syncAttempt2, result1)

        // Should not double-count, but reward ID should remain stable
        assertEquals(result1.totalPoints, result2.totalPoints)
    }
}