package hnau.pinfin.model.transaction.page

import hnau.common.app.model.goback.GoBackHandler
import hnau.pinfin.model.transaction.part.DateModel

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