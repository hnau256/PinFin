package hnau.common.kotlin.coroutines

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class InProgressRegistry {

    private val activitiesCount = MutableStateFlow(0)

    val isProgress = activitiesCount
        .mapStateLite { activitiesCount -> activitiesCount > 0 }

    @PublishedApi
    internal fun incActivitiesCount(delta: Int) {
        activitiesCount.update { it + delta }
    }

    inline fun <R> executeRegistered(
        action: () -> R,
    ): R = try {
        incActivitiesCount(1)
        action()
    } finally {
        incActivitiesCount(-1)
    }
}
