package hnau.common.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListScrollState(
    @SerialName("index")
    val firstVisibleItemIndex: Int = 0,
    @SerialName("offset")
    val firstVisibleItemScrollOffset: Int = 0,
) {

    companion object {
        val initial = ListScrollState()
    }
}
