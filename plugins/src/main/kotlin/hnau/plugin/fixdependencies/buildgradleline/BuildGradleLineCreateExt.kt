package hnau.plugin.fixdependencies.buildgradleline

import hnau.plugin.fixdependencies.path.Path
import hnau.plugin.fixdependencies.path.createModuleIdentifier

fun BuildGradleLine.Companion.create(
    line: String,
): BuildGradleLine = BuildGradleLine.Dependency
    .tryCreate(line)
    ?: BuildGradleLine.Regular(line)

fun BuildGradleLine.Companion.create(lines: Iterable<String>): List<BuildGradleLine> =
    lines.map(BuildGradleLine.Companion::create)

private fun BuildGradleLine.Dependency.Companion.tryCreate(
    line: String,
): BuildGradleLine.Dependency? {
    if (!line.endsWith(')')) {
        return null
    }
    var spaces = 0
    while (spaces < line.length && line[spaces] == ' ') {
        spaces++
    }
    val trimmed = line.substring(spaces)
    val configuration = BuildGradleLine.Dependency.Configuration
        .entries
        .find { configuration -> trimmed.startsWith(configuration.key) }
        ?: return null
    val content = trimmed
        .subSequence(
            startIndex = configuration.key.length + 1,
            endIndex = trimmed.length - 1,
        )
        .toString()
    val type = BuildGradleLine.Dependency.Type.Module.tryCreate(content)
        ?: BuildGradleLine.Dependency.Type.Library.tryCreate(content)
        ?: error("Unable parse dependency from '$line'")
    return BuildGradleLine.Dependency(
        spaces = spaces,
        configuration = configuration,
        type = type,
    )
}

private fun String.removePrefixAndPostfix(
    prefix: String,
    postfix: String,
): String? {
    if (!startsWith(prefix)) {
        return null
    }
    if (!endsWith(postfix)) {
        return null
    }
    return subSequence(
        startIndex = prefix.length,
        endIndex = length - postfix.length,
    ).toString()
}

private fun BuildGradleLine.Dependency.Type.Module.Companion.tryCreate(
    configurationArgument: String,
): BuildGradleLine.Dependency.Type.Module? {
    val moduleIdentifier = configurationArgument
        .removePrefixAndPostfix(
            prefix = "project(\"",
            postfix = "\")",
        )
        ?: return null
    val path = Path.createModuleIdentifier(moduleIdentifier)
    return BuildGradleLine.Dependency.Type.Module(
        identifier = path,
    )
}

private fun BuildGradleLine.Dependency.Type.Library.Companion.tryCreate(
    configurationArgument: String,
): BuildGradleLine.Dependency.Type.Library? {
    val identifier = configurationArgument
        .takeIf { argument ->
            argument.any { char ->
                char.isLetter() || char == '.'
            }
        }
        ?: return null
    return BuildGradleLine.Dependency.Type.Library(
        identifier = identifier,
    )
}