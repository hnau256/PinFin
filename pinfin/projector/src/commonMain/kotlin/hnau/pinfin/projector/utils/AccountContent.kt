package hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import hnau.common.app.projector.uikit.shape.HnauShape
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.projector.resources.Res
import hnau.pinfin.projector.resources.account
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountContent(
    info: AccountInfo?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    shape: Shape = LabelDefaults.shape,
    viewMode: ViewMode = ViewMode.default,
    onClick: (() -> Unit)? = null,
    content: @Composable (inner: @Composable () -> Unit) -> Unit = { inner -> inner() },
) {
    EntityContent(
        entity = info,
        modifier = modifier,
        selected = selected,
        shape = shape,
        viewMode = viewMode,
        content = content,
        onClick = onClick,
        extractHue = AccountInfo::hue,
        extractIcon = { info -> info.icon?.image },
        extractTitle = AccountInfo::title,
        entityTypeName = stringResource(Res.string.account),
    )
}