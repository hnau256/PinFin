package hnau.plugin.fixdependencies.buildgradleline

import hnau.plugin.fixdependencies.path.moduleIdentifier

fun BuildGradleLine.toLine(): String = when (this) {
    is BuildGradleLine.Regular -> line
    is BuildGradleLine.Dependency -> " ".repeat(spaces) +
            configuration.toLinePart(type.toConfigurationArgument())
}

fun Iterable<BuildGradleLine>.toLines(): String = joinToString(
    separator = "\n",
    postfix = "\n",
    transform = BuildGradleLine::toLine,
)


private fun BuildGradleLine.Dependency.Configuration.toLinePart(
    argument: String,
): String = "$key($argument)"

private fun BuildGradleLine.Dependency.Type.toConfigurationArgument(): String = when (this) {
    is BuildGradleLine.Dependency.Type.Library -> identifier
    is BuildGradleLine.Dependency.Type.Module -> "project(\"${identifier.moduleIdentifier}\")"
}