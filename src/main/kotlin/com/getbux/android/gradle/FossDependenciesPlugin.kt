package com.getbux.android.gradle

import com.getbux.android.gradle.render.HtmlRenderer
import com.getbux.android.gradle.task.GenerateFossDependenciesTask
import com.github.jk1.license.LicenseReportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

@Suppress("unused")
class FossDependenciesPlugin : Plugin<Project> {

    lateinit var extension: FossDependenciesExtension
    lateinit var extensionDependencies: FossDependenciesExtension.DependenciesExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("fossDependencies", FossDependenciesExtension::class.java)
        extensionDependencies = (extension as ExtensionAware).extensions.create("dependencies", FossDependenciesExtension.DependenciesExtension::class.java)

        setupReportPlugin(project)

        val reportTask = project.getTasksByName("generateLicenseReport", false).first()
        reportTask.setOnlyIf { true }
        reportTask.outputs.upToDateWhen { false }

        project.task(mapOf("type" to GenerateFossDependenciesTask::class.java), "generateFossDependencies").apply {
            dependsOn(reportTask)
        }
    }

    private fun setupReportPlugin(project: Project) {
        project.apply(mapOf("plugin" to LicenseReportPlugin::class.java))

        val htmlRenderer = HtmlRenderer()

        val reportExtension = project.extensions.getByType(LicenseReportPlugin.LicenseReportExtension::class.java).apply {
            renderer = htmlRenderer
        }

        project.afterEvaluate {
            htmlRenderer.title = extension.htmlTitle
            htmlRenderer.fileDir = reportExtension.outputDir
            htmlRenderer.ignoredDependenciesRegExes = extensionDependencies.ignoredDependencies
            htmlRenderer.manualLicenses = extensionDependencies.manualLicenses
        }
    }

}
