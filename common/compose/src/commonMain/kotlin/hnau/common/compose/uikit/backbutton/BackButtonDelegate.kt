package hnau.common.compose.uikit.backbutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import hnau.common.app.goback.GoBackHandler
import hnau.common.compose.uikit.HnauButton
import hnau.common.compose.utils.AppInsets
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.map
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BackButtonDelegate(
    scope: CoroutineScope,
    private val goBackHandler: GoBackHandler,
    private val dependencies: Dependencies,
) : BackButtonWidthProvider {

    @Shuffle
    interface Dependencies {

        val appInsets: AppInsets
    }

    private val visibilityFraction: Animatable<Float, AnimationVector1D> = Animatable(
        initialValue = 0f,
        typeConverter = Float.VectorConverter
    ).apply {
        scope.launch {
            goBackHandler.collectLatest { goBackOrNull ->
                val target = when (goBackOrNull) {
                    null -> 0f
                    else -> 1f
                }
                animateTo(
                    targetValue = target,
                    animationSpec = TweenSpec(),
                )
            }
        }
    }

    override val backButtonWidthFraction: State<BackButtonWidthFraction> =
        visibilityFraction.asState().map(::BackButtonWidthFraction)

    @Composable
    fun Content() {
        val width = backButtonWidthFraction.value.width
        Box(
            modifier = Modifier
                .padding(top = dependencies.appInsets.insets.calculateTopPadding())
                .size(BackButtonConstants.size)
                .offset(x = width - BackButtonConstants.size),
            contentAlignment = Alignment.Center,
        ) {
            HnauButton(
                modifier = Modifier.size(BackButtonConstants.size),
                onClick = { goBackHandler.value?.invoke() },
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                ) { Icons.AutoMirrored.Filled.ArrowBack }
            }
        }
    }

}