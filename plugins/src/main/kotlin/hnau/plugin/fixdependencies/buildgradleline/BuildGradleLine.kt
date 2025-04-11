package hnau.plugin.fixdependencies.buildgradleline

import hnau.plugin.fixdependencies.path.Path

sealed interface BuildGradleLine {

    data class Regular(
        val line: String,
    ) : BuildGradleLine

    data class Dependency(
        val spaces: Int,
        val configuration: Configuration,
        val type: Type,
    ) : BuildGradleLine {

        enum class Configuration(
            val key: String,
        ) {

            Implementation(
                key = "implementation",
            );
        }

        sealed interface Type {

            data class Library(
                val identifier: String,
            ) : Type {

                companion object
            }

            data class Module(
                val identifier: Path,
            ) : Type {

                companion object
            }
        }

        companion object
    }

    companion object
}