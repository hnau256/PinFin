package hnau.plugin.fixdependencies.path


fun Path.Companion.createModuleIdentifier(
    moduleIdentifier: String,
): Path = mapper.toPath(
    moduleIdentifier,
)

val Path.moduleIdentifier: String
    get() = mapper.fromPath(this)


private val mapper: PathMapper = PathMapper.create(
    prefix = ":",
    separator = ":",
)