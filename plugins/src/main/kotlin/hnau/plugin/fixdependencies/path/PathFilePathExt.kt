package hnau.plugin.fixdependencies.path


fun Path.Companion.createFromFilePath(
    moduleIdentifier: String,
): Path = mapper.toPath(
    moduleIdentifier,
)

val Path.filePath: String
    get() = mapper.fromPath(this)


private val mapper: PathMapper = PathMapper.create(
    prefix = "",
    separator = "/",
)