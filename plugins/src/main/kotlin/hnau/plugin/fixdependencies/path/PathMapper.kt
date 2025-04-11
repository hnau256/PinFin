package hnau.plugin.fixdependencies.path

data class PathMapper(
    val toPath: (String) -> Path,
    val fromPath: (Path) -> String,
) {

    companion object {

        fun create(
            prefix: String,
            separator: String,
        ): PathMapper = PathMapper(
            toPath = { string ->
                string
                    .removePrefix(prefix)
                    .split(separator)
                    .let(::Path)
            },
            fromPath = { path ->
                path
                    .parts
                    .joinToString(
                        separator = separator,
                        prefix = prefix,
                    )
            }
        )
    }
}