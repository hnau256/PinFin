package org.hnau.pinfin.projector.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import org.hnau.commons.app.projector.uikit.ItemsRow
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.SwitchHue
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.model.utils.modelHueToHue


@Fold
enum class ViewMode {
    Full, Icon;

    companion object {

        val default: ViewMode
            get() = Icon
    }
}

data class EntityUiInfo(
    val hue: Hue,
    val icon: EntityUiInfo.Icon?,
    val title: String,
) {

    data class Icon(
        val main: ImageVector,
        val additional: ImageVector? = null,
    )
}

private val absentEntityUiInfoIcon = EntityUiInfo.Icon(
    main = UIConstants.absentValueIcon,
)

@Composable
fun EntityContent(
    uiInfo: EntityUiInfo?,
    entityTypeName: String,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    shape: Shape = LabelDefaults.shape,
    viewMode: ViewMode = ViewMode.default,
    content: @Composable (inner: @Composable () -> Unit) -> Unit = { inner -> inner() },
    onClick: (() -> Unit)?,
) {
    uiInfo.foldNullable(
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
                            icon = absentEntityUiInfoIcon,
                            title = entityTypeName,
                            viewMode = viewMode,
                        ),
                    )
                }
            }
        },
        ifNotNull = { info ->
            SwitchHue(
                hue = info.hue.let(Mapper.modelHueToHue.reverse),
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
                                icon = info.icon,
                                title = info.title,
                                viewMode = viewMode,
                            ),
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
    ItemsRow(
        modifier = modifier,
    ) {
        when (state) {
            IconWithTitleState.Empty -> Unit
            is IconWithTitleState.Icon -> Icon(
                icon = state.icon,
            )

            is IconWithTitleState.IconWithTitle -> {
                Icon(
                    icon = state.icon,
                )
                Title(
                    text = state.title,
                )
            }

            is IconWithTitleState.Title -> Title(
                text = state.title,
            )

            is IconWithTitleState.TitleAsIcon -> Text(
                text = state.titleFirstChars,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Icon(
    icon: EntityUiInfo.Icon,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        icon = icon.main,
    )
}


@Immutable
private sealed interface IconWithTitleState {

    @Immutable
    data class IconWithTitle(
        val icon: EntityUiInfo.Icon,
        val title: String,
    ) : IconWithTitleState

    @Immutable
    data class Title(
        val title: String,
    ) : IconWithTitleState

    @Immutable
    data class Icon(
        val icon: EntityUiInfo.Icon,
    ) : IconWithTitleState

    @Immutable
    data class TitleAsIcon(
        val titleFirstChars: String,
    ) : IconWithTitleState

    @Immutable
    data object Empty : IconWithTitleState

    companion object {

        fun create(
            icon: EntityUiInfo.Icon?,
            title: String,
            viewMode: ViewMode,
        ): IconWithTitleState = viewMode.fold(
            ifFull = {
                icon.foldNullable(
                    ifNull = { Title(title) },
                    ifNotNull = { icon ->
                        IconWithTitle(
                            icon = icon,
                            title = title,
                        )
                    }
                )
            },
            ifIcon = {
                icon.foldNullable(
                    ifNotNull = ::Icon,
                    ifNull = {
                        title
                            .extractNChars(2)
                            .takeIf(String::isNotEmpty)
                            .foldNullable(
                                ifNull = { Empty },
                                ifNotNull = ::TitleAsIcon,
                            )
                    }
                )
            },
        )

        @Composable
        fun remember(
            icon: EntityUiInfo.Icon?,
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

private fun String.extractNChars(
    n: Int,
): String = this
    .split(' ')
    .filter(String::isNotEmpty)
    .let { words ->
        words
            .foldIndexed("" to n) { index, (acc, rem), word ->

                val left = words.size - index

                val limit = (rem + left - 1) / left

                val taken = word
                    .take(limit)
                    .replaceFirstChar(Char::uppercaseChar)

                (acc + taken) to (rem - taken.length)
            }
            .first
    }

