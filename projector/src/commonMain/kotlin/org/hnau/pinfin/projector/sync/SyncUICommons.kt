package org.hnau.pinfin.projector.sync

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Security
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.pinfin.projector.Localization

internal object SyncUICommons {

    fun createSchemeTitleWithIcon(
        localization: Localization,
    ): TitleOrIcon.Both = TitleOrIcon.Both(
        title = localization.httpScheme,
        icon = Drawable.Vector(
            Icons.Default.Security,
        )
    )

    fun createHostTitleWithIcon(
        localization: Localization,
    ): TitleOrIcon.Both = TitleOrIcon.Both(
        title = localization.serverHost,
        icon = Drawable.Vector(
            Icons.Default.Cloud,
        )
    )

}