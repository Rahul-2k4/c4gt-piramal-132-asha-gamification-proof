package proof.gamification

data class GamificationEvent(
    val eventId: String,
    val workerId: String,
    val actionType: String,
    val points: Int,
    val localDate: String,
    val syncStatus: String = "pending",
    val rewardId: String? = null
)

data class WorkerProgress(
    val acceptedEventIds: Set<String> = emptySet(),
    val dailyPoints: Map<String, Int> = emptyMap(),
    val totalPoints: Int = 0
)

class GamificationRuleEngine {
    companion object {
        const val DAILY_CAP = 100
    }

    fun apply(event: GamificationEvent, progress: WorkerProgress): WorkerProgress {
        if (event.eventId in progress.acceptedEventIds) {
            return progress.copy(acceptedEventIds = progress.acceptedEventIds + event.eventId)
        }

        val currentDayPoints = progress.dailyPoints[event.localDate] ?: 0

        if (currentDayPoints >= DAILY_CAP) {
            return progress.copy(
                acceptedEventIds = progress.acceptedEventIds + event.eventId,
                dailyPoints = progress.dailyPoints
            )
        }

        val availablePoints = DAILY_CAP - currentDayPoints
        val pointsToAdd = minOf(event.points, availablePoints)

        return progress.copy(
            acceptedEventIds = progress.acceptedEventIds + event.eventId,
            dailyPoints = progress.dailyPoints + (event.localDate to currentDayPoints + pointsToAdd),
            totalPoints = progress.totalPoints + pointsToAdd
        )
    }
}