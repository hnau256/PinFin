package hnau.plugin.fixdependencies.path


fun Path.Companion.createFromPackage(
    packageString: String,
): Path = mapper.toPath(
    packageString,
)

val Path.packagePrefix: String
    get() = mapper.fromPath(this)


private val mapper: PathMapper = PathMapper.create(
    prefix = "hnau.",
    separator = ".",
)