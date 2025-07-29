package hnau.pinfin.projector.utils.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.app.projector.uikit.utils.Dimens
import hnau.common.app.projector.utils.Icon
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.projector.utils.SwitchHueToCategoryInfo
import hnau.pinfin.projector.utils.image

@Composable
fun CategoryContent(
    info: CategoryInfo,
    modifier: Modifier = Modifier,
) {
    SwitchHueToCategoryInfo(
        info = info,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.extraSmallSeparation),
        ) {
            info
                .icon
                ?.let { icon ->
                    Icon(
                        icon = icon.image,
                    )
                }
            Text(
                text = info.title,
                maxLines = 1,
            )
        }

    }
}