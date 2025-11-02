package hnau.pinfin.projector.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hct.Hct
import hnau.common.app.model.theme.ThemeBrightness
import hnau.common.app.model.theme.ThemeBrightnessValues
import hnau.common.app.projector.utils.collectAsMutableAccessor
import hnau.common.app.projector.utils.theme.DynamicSchemeConfig
import hnau.common.app.projector.utils.theme.themeBrightness
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.model
import kotlinx.coroutines.flow.MutableStateFlow
import hnau.common.app.model.utils.Hue as ModelHue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HueSlider(
    value: MutableStateFlow<Hue>,
    modifier: Modifier = Modifier,
) {
    var current by value.collectAsMutableAccessor()
    val brightness = MaterialTheme.themeBrightness
    Slider(
        value = current.degrees.toFloat(),
        onValueChange = { newValue ->
            current = newValue.toInt().let(::Hue)
        },
        valueRange = 0f..359f,
        modifier = modifier,
        colors = SliderDefaults.colors(
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape,
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .background(
                            color = calcColor(
                                brightness = brightness,
                                hue = current.model,
                            ),
                            shape = CircleShape,
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = CircleShape,
                            )
                    )
                }
            }
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        brush = rainbowBrushes[brightness],
                        shape = RoundedCornerShape(100f),
                    )
            )
        }
    )
}


private val dynamicColorsConfig = DynamicSchemeConfig(
    tone = ThemeBrightnessValues(
        light = 40.0,
        dark = 60.0,
    ),
    chroma = 100.0,
)

private fun calcColor(
    brightness: ThemeBrightness,
    hue: ModelHue,
): Color {
    return Hct
        .from(
            /* hue = */ hue.degrees.toDouble(),
            /* chroma = */ dynamicColorsConfig.chroma,
            /* tone = */ dynamicColorsConfig.tone[brightness],
        )
        .toInt()
        .let(::Color)
}

private val rainbowBrushes: ThemeBrightnessValues<Brush> = run {
    val pointsCount = 360
    ThemeBrightnessValues { brightness ->
        Brush.horizontalGradient(
            colors = (0 until pointsCount).map { i ->
                calcColor(
                    brightness = brightness,
                    hue = ModelHue(
                        degrees = (360.0 * i.toDouble() / (pointsCount - 1)).toInt(),
                    ),
                )
            }
        )
    }
}