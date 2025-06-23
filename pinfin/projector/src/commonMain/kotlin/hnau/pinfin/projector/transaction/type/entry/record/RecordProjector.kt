package hnau.pinfin.projector.transaction.type.entry.record

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.projector.uikit.state.StateContent
import hnau.common.projector.uikit.state.TransitionSpec
import hnau.pinfin.model.transaction.type.entry.record.RecordModel
import hnau.pinfin.projector.transaction.type.utils.ChooseCategoryProjector
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class RecordProjector(
    private val scope: CoroutineScope,
    private val model: RecordModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        fun mainDelegate(): RecordProjectorMainDelegate.Dependencies

        fun chooseCategory(): ChooseCategoryProjector.Dependencies
    }

    private sealed interface Overlap {

        data class ChooseCategory(
            val chooseCategoryProjector: ChooseCategoryProjector,
        ) : Overlap

        data class Remove(
            val delegate: RecordProjectorRemoveDelegate,
        ) : Overlap
    }

    private val overlap: StateFlow<Overlap?> = model
        .overlap
        .mapWithScope(
            scope = scope,
        ) { stateScope, overlapOrNull ->
            overlapOrNull?.let { overlap ->
                when (overlap) {
                    is RecordModel.OverlapDialogModel.ChooseCategory -> Overlap.ChooseCategory(
                        chooseCategoryProjector = ChooseCategoryProjector(
                            scope = stateScope,
                            model = overlap.chooseCategoryModel,
                            dependencies = dependencies.chooseCategory(),
                        )
                    )

                    is RecordModel.OverlapDialogModel.Remove -> Overlap.Remove(
                        delegate = RecordProjectorRemoveDelegate(
                            remove = overlap.remove,
                            model = model,
                        ),
                    )
                }
            }
        }

    private val mainDelegate = RecordProjectorMainDelegate(
        scope = scope,
        dependencies = dependencies.mainDelegate(),
        model = model,
    )

    @Composable
    fun Content() {
        overlap
            .collectAsState()
            .value
            .StateContent(
                transitionSpec = TransitionSpec.crossfade(),
                label = "ChooseCategoryOrRecord",
                contentKey = {
                    when (it) {
                        null -> 0
                        is Overlap.Remove -> 1
                        is Overlap.ChooseCategory -> 2
                    }
                },
            ) { overlap ->
                when (overlap) {
                    null -> mainDelegate.Content()

                    is Overlap.ChooseCategory -> overlap.chooseCategoryProjector.Content()

                    is Overlap.Remove -> overlap.delegate.Content()
                }
            }
    }
}