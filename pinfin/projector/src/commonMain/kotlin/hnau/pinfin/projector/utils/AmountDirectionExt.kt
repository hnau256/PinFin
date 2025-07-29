package hnau.pinfin.projector.utils

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import hnau.common.app.model.theme.Hue
import hnau.common.app.projector.utils.theme.DynamicSchemeConfig
import hnau.common.app.projector.utils.theme.rememberColorScheme
import hnau.pinfin.data.AmountDirection

val AmountDirection.hue: Hue
    get() = when (this) {
        AmountDirection.Credit -> Hue(0.375f)
        AmountDirection.Debit -> Hue(0.075f)
    }

@Composable
fun SwitchHueToAmountDirection(
    amountDirection: AmountDirection,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = rememberColorScheme(
            hue = amountDirection.hue,
            config = DynamicSchemeConfig.forHue,
        ).copyContainerToBase(),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.primary
        ) {
            content()
        }
    }
}