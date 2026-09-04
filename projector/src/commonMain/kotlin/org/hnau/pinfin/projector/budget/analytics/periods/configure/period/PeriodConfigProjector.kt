package org.hnau.pinfin.projector.budget.analytics.periods.configure.period

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.hnau.commons.app.projector.uikit.row.ChipsFlowRow
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period.PeriodConfigModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.Label

/**
 * Пресеты периода + "Свой" + якорь (docs/analytics-v2-plan.md, "2.2", "2.6",
 * `PeriodConfigModel`). Якорь показывается для любого пресета, кроме "Весь период" -
 * так решается проблема 1 из плана ("начало периода нельзя задать").
 */
class PeriodConfigProjector(
    private val model: PeriodConfigModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun duration(): DurationProjector.Dependencies

        fun anchor(): AnchorProjector.Dependencies
    }

    private val duration = DurationProjector(
        model = model.duration,
        dependencies = dependencies.duration(),
    )

    private val anchor = AnchorProjector(
        model = model.anchor,
        dependencies = dependencies.anchor(),
    )

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Dimens.separation),
        ) {
            val preset = model.preset.collectAsState().value
            ChipsFlowRow(
                all = PeriodConfigModel.Preset.entries,
            ) { item ->
                Label(
                    selected = item == preset,
                    onClick = { model.preset.value = item },
                ) {
                    Text(presetTitle(item))
                }
            }
            AnimatedVisibility(visible = preset == PeriodConfigModel.Preset.Custom) {
                duration.Content(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AnimatedVisibility(visible = preset != PeriodConfigModel.Preset.Whole) {
                anchor.Content(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    private fun presetTitle(
        preset: PeriodConfigModel.Preset,
    ): String = dependencies.localization.let { l ->
        when (preset) {
            PeriodConfigModel.Preset.Week -> l.presetWeek
            PeriodConfigModel.Preset.Month -> l.month.replaceFirstChar(Char::uppercase)
            PeriodConfigModel.Preset.Quarter -> l.presetQuarter
            PeriodConfigModel.Preset.Year -> l.year.replaceFirstChar(Char::uppercase)
            PeriodConfigModel.Preset.Whole -> l.inclusivePeriod
            PeriodConfigModel.Preset.Custom -> l.presetCustom
        }
    }
}
