package hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import hnau.common.app.projector.uikit.ItemsRow
import hnau.common.app.projector.utils.Icon
import hnau.common.app.projector.utils.SwitchHue
import hnau.common.kotlin.foldNullable
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.model


enum class ViewMode {
    Full, Icon;

    companion object {

        val default: ViewMode
            get() = Full
    }
}

@Composable
fun <T : Any> EntityContent(
    entity: T?,
    extractHue: (T) -> Hue,
    extractIcon: (T) -> ImageVector?,
    extractTitle: (T) -> String,
    entityTypeName: String,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    shape: Shape = LabelDefaults.shape,
    viewMode: ViewMode = ViewMode.default,
    content: @Composable (inner: @Composable () -> Unit) -> Unit = { inner -> inner() },
    onClick: (() -> Unit)?,
) {
    entity.foldNullable(
        ifNull = {
            Label(
                modifier = modifier,
                containerColor = UIConstants.absentValueColor,
                selected = selected,
                onClick = onClick,
                shape = shape,
            ) {
                content {
                    IconWithTitle(
                        state = IconWithTitleState.remember(
                            icon = UIConstants.absentValueIcon,
                            title = entityTypeName,
                            viewMode = viewMode,
                        )
                    )
                }
            }
        },
        ifNotNull = { existingEntity ->
            SwitchHue(
                hue = extractHue(existingEntity).model,
            ) {
                Label(
                    modifier = modifier,
                    selected = selected,
                    onClick = onClick,
                    shape = shape,
                ) {
                    content {
                        IconWithTitle(
                            state = IconWithTitleState.remember(
                                icon = extractIcon(existingEntity),
                                title = extractTitle(existingEntity),
                                viewMode = viewMode,
                            )
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun IconWithTitle(
    state: IconWithTitleState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        IconWithTitleState.Empty -> Unit
        is IconWithTitleState.Icon -> Icon(
            modifier = modifier,
            icon = state.icon,
        )

        is IconWithTitleState.IconWithTitle -> ItemsRow(
            modifier = modifier,
        ) {
            Icon(
                icon = state.icon,
            )
            Title(
                text = state.title,
            )
        }

        is IconWithTitleState.Title -> Title(
            text = state.title,
            modifier = modifier,
        )

        is IconWithTitleState.TitleAsIcon -> Text(
            modifier = modifier,
            text = state.titleFirstChar.toString(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}


@Immutable
private sealed interface IconWithTitleState {

    @Immutable
    data class IconWithTitle(
        val icon: ImageVector,
        val title: String,
    ) : IconWithTitleState

    @Immutable
    data class Title(
        val title: String,
    ) : IconWithTitleState

    @Immutable
    data class Icon(
        val icon: ImageVector,
    ) : IconWithTitleState

    @Immutable
    data class TitleAsIcon(
        val titleFirstChar: Char,
    ) : IconWithTitleState

    @Immutable
    data object Empty : IconWithTitleState

    companion object {

        fun create(
            icon: ImageVector?,
            title: String,
            viewMode: ViewMode,
        ): IconWithTitleState = when (viewMode) {
            ViewMode.Full -> icon.foldNullable(
                ifNull = { Title(title) },
                ifNotNull = { icon -> IconWithTitle(icon, title) }
            )

            ViewMode.Icon -> icon.foldNullable(
                ifNotNull = ::Icon,
                ifNull = {
                    title
                        .firstOrNull()
                        .foldNullable(
                            ifNull = { Empty },
                            ifNotNull = ::TitleAsIcon,
                        )
                }
            )
        }

        @Composable
        fun remember(
            icon: ImageVector?,
            title: String,
            viewMode: ViewMode,
        ): IconWithTitleState = remember(icon, title, viewMode) {
            create(
                icon = icon,
                title = title,
                viewMode = viewMode,
            )
        }
    }
}

@Composable
private fun Title(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = 1,
        style = MaterialTheme.typography.bodyLarge,
    )
}