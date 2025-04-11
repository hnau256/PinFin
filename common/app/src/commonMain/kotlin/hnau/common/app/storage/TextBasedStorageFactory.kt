package hnau.common.app.storage

fun TextBasedStorageFactory(
    read: suspend () -> String?,
    write: suspend (String) -> Unit,
): Storage.Factory = Storage.Factory {
    val initialValues: String? = read()
    Storage.textBased(
        initial = initialValues,
        update = write,
    )
}

fun Storage.Factory.Companion.textBased(
    read: suspend () -> String?,
    write: suspend (String) -> Unit,
): Storage.Factory = TextBasedStorageFactory(
    read,
    write,
)
