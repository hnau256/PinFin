package hnau.common.app.goback

interface GoBackHandlerProvider {

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler

    companion object {

        val never: GoBackHandlerProvider = object : GoBackHandlerProvider {}
    }
}
