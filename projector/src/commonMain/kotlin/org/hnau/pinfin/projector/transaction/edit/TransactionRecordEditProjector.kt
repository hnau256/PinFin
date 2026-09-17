package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.hnau.commons.app.model.utils.fold
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.uikit.state.BooleanStateContent
import org.hnau.commons.app.projector.uikit.transition.TransitionSpec
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.ifFalse
import org.hnau.pinfin.model.transaction.edit.TransactionRecordEditModel
import org.hnau.pinfin.model.utils.resolvedDirection
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter

class TransactionRecordEditProjector(
    val model: TransactionRecordEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val amountFormatter: AmountFormatter

        fun comment(): CommentEditProjector.Dependencies

        fun category(): CategoryChooseEditProjector.Dependencies

        fun amount(): AmountEditProjector.Dependencies
    }

    private val comment = CommentEditProjector(
        model = model.comment,
        dependencies = dependencies.comment(),
    )

    private val category = CategoryChooseEditProjector(
        model = model.category,
        dependencies = dependencies.category(),
    )

    private val amount = AmountEditProjector(
        model = model.amount,
        dependencies = dependencies.amount(),
    )

    @Composable
    fun Content(
        selected: Boolean,
        modifier: Modifier = Modifier,
    ) {
        SPanel(
            modifier = modifier.fillMaxWidth(),
            actionOrElseOrDisabled = selected.ifFalse {
                ActionOrElse.instant {
                    model.comment.navigateContext.requestFocus()
                }
            },
        ) {
            selected.BooleanStateContent(
                transitionSpec = TransitionSpec.remember(
                    showAlignment = Alignment.TopCenter,
                ),
                falseContent = {
                    Collapsed()
                },
                trueContent = {
                    Expanded()
                },
            )
        }
    }

    @Composable
    private fun Expanded() {
        val remove by model.remove.collectAsState()
        SLine(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            separation = LocalDistance.current.units.padding.along.small,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalDistance.current.units.padding.along.small),
            ) {
                comment.Content()
                category.Content()
                amount.Content()
            }
            remove?.let { onClick ->
                SPanel(
                    actionOrElseOrDisabled = ActionOrElse.instant(onClick),
                ) {
                    SIcon(Drawable.Vector(Icons.Default.Delete))
                }
            }
        }
    }

    @Composable
    private fun Collapsed() {
        val currency by model.amount.currency.collectAsState()
        val record by model.recordEditable.collectAsState()
        val recordValue = record.fold(
            ifIncorrect = { null },
            ifValue = { value, _ -> value },
        )
        SLine(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            separation = LocalDistance.current.units.padding.along.small,
        ) {
            SText(
                modifier = Modifier.weight(1f),
                text = recordValue?.category?.value?.title.orEmpty(),
            )
            recordValue?.amount
                ?.toAmount(currency.scale)
                ?.let { value ->
                    AmountContent(
                        value = KeyValue(
                            key = recordValue.resolvedDirection,
                            value = value,
                        ),
                        amountFormatter = dependencies.amountFormatter,
                    )
                }
        }
    }
}
