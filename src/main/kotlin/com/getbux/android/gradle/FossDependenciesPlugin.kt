package com.getbux.android.gradle

import com.getbux.android.gradle.render.HtmlRenderer
import com.getbux.android.gradle.render.ManualLicense
import com.getbux.android.gradle.task.GenerateFossDependenciesTask
import com.github.jk1.license.LicenseReportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class FossDependenciesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = Extension()
        project.extensions.add("fossDependencies", extension)

        setupReportPlugin(project, extension)

        val reportTask = project.getTasksByName("generateLicenseReport", false).first()
        reportTask.setOnlyIf { true }
        reportTask.outputs.upToDateWhen { false }

        project.task(mapOf("type" to GenerateFossDependenciesTask::class.java), "generateFossDependencies").apply {
            dependsOn(reportTask)
        }
    }

    private fun setupReportPlugin(project: Project, extension: Extension) {
        project.apply(mapOf("plugin" to LicenseReportPlugin::class.java))

        val htmlRenderer = HtmlRenderer()

        val reportExtension = project.extensions.getByType(LicenseReportPlugin.LicenseReportExtension::class.java).apply {
            renderer = htmlRenderer
        }

        project.afterEvaluate {
            htmlRenderer.title = extension.htmlTitle
            htmlRenderer.fileDir = reportExtension.outputDir
            htmlRenderer.ignoredDependenciesRegExes = extension.ignoredDependencies.map { Regex(it.replace("*", ".*")) }
            htmlRenderer.manualLicenses = extension.manualLicenses.mapKeys { Regex(it.key.replace("*", ".*")) }
        }
    }

    class Extension {
        var outputFile: String? = null
        var htmlTitle: String? = null
        var ignoredDependencies: Array<String> = emptyArray()
        var manualLicenses: Map<String, ManualLicense> = emptyMap()
    }

}
