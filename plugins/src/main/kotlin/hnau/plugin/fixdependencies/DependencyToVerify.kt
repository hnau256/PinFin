package hnau.plugin.fixdependencies

import arrow.core.NonEmptyList
import arrow.core.NonEmptySet
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import hnau.plugin.fixdependencies.buildgradleline.BuildGradleLine
import hnau.plugin.fixdependencies.path.packagePrefix

data class DependencyToVerify(
    val dependency: BuildGradleLine.Dependency.Type,
    val importsPrefixes: NonEmptySet<String>,
) {

    companion object {

        fun collect(
            lines: List<BuildGradleLine>,
            librariesPrefixes: Map<String, List<String>>,
        ): List<DependencyToVerify> = lines
            .filterIsInstance<BuildGradleLine.Dependency>()
            .map(BuildGradleLine.Dependency::type)
            .mapNotNull { type ->
                val packagesPrefixes: NonEmptyList<String> = when (type) {
                    is BuildGradleLine.Dependency.Type.Library -> {
                        val identifier = type.identifier
                        val packagesPrefixes = librariesPrefixes[identifier]
                            ?: error("Unknown packages for library `$identifier`")
                        packagesPrefixes
                            .toNonEmptyListOrNull()
                            ?: return@mapNotNull null

                    }

                    is BuildGradleLine.Dependency.Type.Module -> nonEmptyListOf(type.identifier.packagePrefix)
                }
                DependencyToVerify(
                    dependency = type,
                    importsPrefixes = packagesPrefixes
                        .map { "import $it" }
                        .toNonEmptySet(),
                )
            }
    }
}