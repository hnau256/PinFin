package hnau.common.compose.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.topappbar.TopAppBar
import hnau.common.compose.uikit.topappbar.TopAppBarDependencies
import hnau.common.compose.uikit.topappbar.TopAppBarScope
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.AppInsets
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface ScreenContentDependencies {

    fun topAppBar(): TopAppBarDependencies

    val appInsets: AppInsets
}

@Composable
fun ScreenContent(
    dependencies: ScreenContentDependencies,
    topAppBarContent: @Composable TopAppBarScope.() -> Unit,
    content: @Composable (
        contentPadding: PaddingValues,
    ) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val insets = dependencies.appInsets.insets
    val (appBarPaddings, contentPaddings) = run {
        val top = insets.calculateTopPadding()
        val bottom = insets.calculateBottomPadding()
        val start = insets.calculateStartPadding(layoutDirection)
        val end = insets.calculateStartPadding(layoutDirection)
        val appBarPaddings = PaddingValues(
            start = start,
            end = end,
            top = top,
            bottom = 0.dp,
        )
        val contentPaddings = PaddingValues(
            start = start,
            end = end,
            top = top + Dimens.rowHeight,
            bottom = bottom,
        )
        appBarPaddings to contentPaddings
    }
    content(contentPaddings)
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        TopAppBar(
            dependencies = remember(dependencies) { dependencies.topAppBar() },
            padding = appBarPaddings,
            content = topAppBarContent,
        )
    }
}