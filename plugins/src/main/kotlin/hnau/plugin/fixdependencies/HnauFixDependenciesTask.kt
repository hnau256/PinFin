package hnau.plugin.fixdependencies

import hnau.plugin.fixdependencies.buildgradleline.BuildGradleLine
import hnau.plugin.fixdependencies.buildgradleline.create
import hnau.plugin.fixdependencies.buildgradleline.prettify
import hnau.plugin.fixdependencies.buildgradleline.toLines
import hnau.plugin.fixdependencies.path.Path
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

abstract class HnauFixDependenciesTask : DefaultTask() {

    @TaskAction
    fun execute() {

        val rootProject = project.rootProject

        val librariesPrefixes: Map<String, List<String>> = getLibraryPackagesPrefixes(rootProject)

        val projectsToAnalyze = collectProjectWithChildren(
            project = rootProject,
            projectPath = Path.empty,
        )
        projectsToAnalyze.forEach { project ->

            val unusedDependencies: MutableList<DependencyToVerify> = DependencyToVerify
                .collect(
                    lines = project.buildGradleLines,
                    librariesPrefixes = librariesPrefixes,
                )
                .toMutableList()

            visitKotlinFiles(
                projectRoot = project.project.projectDir,
                moduleIdentifier = project.path,
            ) { lines ->
                handleKtFile(
                    lines = lines,
                    unusedDependencies = unusedDependencies,
                )
            }
            val buildGradleLines = when (unusedDependencies.isEmpty()) {
                true -> project.buildGradleLines
                false -> {
                    val dependenciesToExclude = unusedDependencies
                        .map { it.dependency }
                        .toSet()
                    project.buildGradleLines.filter { line ->
                        when (line) {
                            is BuildGradleLine.Regular -> true
                            is BuildGradleLine.Dependency -> line.type !in dependenciesToExclude
                        }
                    }
                }
            }
            project.project.buildFile.writeText(
                buildGradleLines.toLines(),
            )
        }
    }

    private fun handleKtFile(
        lines: List<String>,
        unusedDependencies: MutableList<DependencyToVerify>,
    ) {
        lines.forEach { line ->
            unusedDependencies.removeAll { dependencyToVerify ->
                dependencyToVerify.importsPrefixes.any { importsPrefix ->
                    line.startsWith(importsPrefix)
                }
            }
        }
    }

    private data class ProjectToAnalyze(
        val path: Path,
        val project: Project,
        val buildGradleLines: List<BuildGradleLine>,
    )

    private fun collectProjectWithChildren(
        project: Project,
        projectPath: Path,
    ): List<ProjectToAnalyze> = buildList {
        project
            .buildFile
            .takeIf { it.exists() }
            ?.readLines()
            ?.let(BuildGradleLine.Companion::create)
            ?.prettify()
            ?.let { buildGradleLines ->
                add(
                    ProjectToAnalyze(
                        path = projectPath,
                        project = project,
                        buildGradleLines = buildGradleLines,
                    )
                )
            }
        addAll(
            project.childProjects.flatMap { (childSuffix, project) ->
                collectProjectWithChildren(
                    project = project,
                    projectPath = projectPath + childSuffix,
                )
            }
        )
    }
}