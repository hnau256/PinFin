package hnau.plugin.fixdependencies

import org.gradle.api.Plugin
import org.gradle.api.Project

class HnauFixDependenciesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.register(
            "fixDependencies",
            HnauFixDependenciesTask::class.java,
        )
    }
}