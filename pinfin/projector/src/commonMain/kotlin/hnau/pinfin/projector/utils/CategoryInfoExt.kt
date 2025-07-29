package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import hnau.common.app.projector.utils.theme.DynamicSchemeConfig
import hnau.common.app.projector.utils.theme.rememberColorScheme
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.common.app.model.theme.Hue as ModelHue

val CategoryInfo.modelHue: ModelHue
    get() = ModelHue(hue.value)

@Composable
fun SwitchHueToCategoryInfo(
    info: CategoryInfo,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = rememberColorScheme(
            hue = info.modelHue,
            config = DynamicSchemeConfig.forHue,
        ).copyContainerToBase(),
        content = content,
    )
}