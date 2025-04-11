package hnau.plugin.fixdependencies.buildgradleline

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import hnau.plugin.fixdependencies.path.moduleIdentifier

fun Iterable<BuildGradleLine>.prettify(): List<BuildGradleLine> = this
    .fold(
        initial = emptyList<BuildGradleLine>() to true,
    ) { (acc, emptyLineCanBeSkipped), element ->

        fun useElement(): Boolean {
            if (!emptyLineCanBeSkipped) {
                return true
            }
            return when (element) {
                is BuildGradleLine.Dependency -> true
                is BuildGradleLine.Regular -> element.line.isNotBlank()
            }
        }

        if (useElement()) {
            val nextEmptyLineCanBeSkipped = when (element) {
                is BuildGradleLine.Dependency -> true
                is BuildGradleLine.Regular -> false
            }
            (acc + element) to nextEmptyLineCanBeSkipped
        } else {
            acc to true
        }
    }
    .first
    .fold(
        initial = emptyList<Either<BuildGradleLine.Regular, NonEmptyList<BuildGradleLine.Dependency>>>(),
    ) { acc, line ->
        if (acc.isEmpty()) {
            return@fold listOf(
                when (line) {
                    is BuildGradleLine.Dependency -> Either.Right(nonEmptyListOf(line))
                    is BuildGradleLine.Regular -> Either.Left(line)
                }
            )
        }
        when (line) {
            is BuildGradleLine.Regular -> acc + Either.Left(line)
            is BuildGradleLine.Dependency -> {
                when (val tail = acc.last()) {
                    is Either.Left -> acc + Either.Right(nonEmptyListOf(line))
                    is Either.Right -> acc.dropLast(1) + Either.Right(tail.value + line)
                }
            }
        }
    }
    .map { item ->
        item.map { dependencies ->
            dependencies
                .sortedWith(
                    compareBy(
                        { dependency -> dependency.configuration.key },
                        { dependency ->
                            when (val type = dependency.type) {
                                is BuildGradleLine.Dependency.Type.Library ->
                                    "0" + type.identifier

                                is BuildGradleLine.Dependency.Type.Module ->
                                    "1" + type.identifier.moduleIdentifier
                            }
                        },
                    )
                )
                .distinct()
        }
    }
    .flatMap { item ->
        when (item) {
            is Either.Left -> listOf(item.value)
            is Either.Right -> item.value
        }
    }