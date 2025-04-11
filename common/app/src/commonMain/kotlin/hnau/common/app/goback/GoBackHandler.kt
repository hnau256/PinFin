package hnau.common.app.goback

import kotlinx.coroutines.flow.StateFlow

typealias GoBackHandler = StateFlow<(() -> Unit)?>
