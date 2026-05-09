package proof.gamification

data class GamificationEvent(
    val eventId: String,
    val workerId: String,
    val actionType: String,
    val points: Int,
    val localDate: String,
    val syncStatus: String = "pending"
)

data class RewardState(
    val acceptedEventIds: Set<String> = emptySet(),
    val dailyPoints: Map<String, Int> = emptyMap(),
    val totalPoints: Int = 0
)

object RewardRuleEngine {
    const val DAILY_CAP = 100

    fun applyEvent(state: RewardState, event: GamificationEvent): RewardState {
        if (event.eventId in state.acceptedEventIds) return state

        val currentDayPoints = state.dailyPoints[event.localDate] ?: 0
        val allowedPoints = minOf(event.points, DAILY_CAP - currentDayPoints)
        if (allowedPoints <= 0) {
            return state.copy(acceptedEventIds = state.acceptedEventIds + event.eventId)
        }

        return state.copy(
            acceptedEventIds = state.acceptedEventIds + event.eventId,
            dailyPoints = state.dailyPoints + (event.localDate to currentDayPoints + allowedPoints),
            totalPoints = state.totalPoints + allowedPoints
        )
    }
}