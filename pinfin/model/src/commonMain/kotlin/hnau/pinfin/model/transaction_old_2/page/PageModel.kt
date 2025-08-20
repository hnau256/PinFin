package hnau.pinfin.model.transaction_old_2.page

import hnau.common.app.model.goback.GoBackHandler

sealed interface PageModel {

    val goBackHandler: GoBackHandler

    data class Date(
        val model: DatePageModel,
    ): PageModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Time(
        val model: TimePageModel,
    ): PageModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Comment(
        val model: CommentPageModel,
    ): PageModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }

    data class Type(
        val model: TypePageModel,
    ): PageModel {

        override val goBackHandler: GoBackHandler
            get() = model.goBackHandler
    }
}