package hnau.pinfin.client.app

import hnau.common.app.goback.GoBackHandler
import hnau.common.kotlin.mapper.toMapper
import hnau.pinfin.client.model.RootModel
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

class PinFinApp(
    scope: CoroutineScope,
    savedState: SavedState,
    dependencies: Dependencies,
) {

    @Shuffle
    interface Dependencies {

        fun root(): RootModel.Dependencies

        companion object
    }

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val modelSkeletonMapper =
        json.toMapper(RootModel.Skeleton.serializer())

    private val modelSkeleton = savedState
        .savedState
        ?.let(modelSkeletonMapper.direct)
        ?: RootModel.Skeleton()

    val model = RootModel(
        scope = scope,
        skeleton = modelSkeleton,
        dependencies = dependencies.root(),
    )

    val savableState: SavedState
        get() = modelSkeletonMapper.reverse(modelSkeleton).let(::SavedState)

    val goBackHandler: GoBackHandler
        get() = model.goBackHandler
}
