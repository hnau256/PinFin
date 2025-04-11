package hnau.common.compose.uikit.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

class TopAppBarScopeImpl(
    parent: RowScope,
) : TopAppBarScope, RowScope by parent {

    @Composable
    override fun Title(
        text: String,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            textAlign = TextAlign.Start,
            maxLines = 1,
        )
    }

    @Composable
    override fun Action(
        onClick: (() -> Unit)?,
        content: @Composable RowScope.() -> Unit,
    ) {
        TextButton(
            onClick = { onClick?.invoke() },
            enabled = onClick != null,
            content = content,
        )
    }
}