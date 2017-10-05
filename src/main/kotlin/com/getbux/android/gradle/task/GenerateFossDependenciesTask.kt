package com.getbux.android.gradle.task

import com.getbux.android.gradle.FossDependenciesExtension
import com.getbux.android.gradle.render.HtmlRenderer
import com.github.jk1.license.LicenseReportPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.InvalidParameterException

open class GenerateFossDependenciesTask : DefaultTask() {

    private val reportExtension by lazy { project.extensions.getByType(LicenseReportPlugin.LicenseReportExtension::class.java) }
    private val fossDependenciesExtension by lazy { project.extensions.getByType(FossDependenciesExtension::class.java) }
    private val htmlRenderer by lazy { reportExtension.renderer as? HtmlRenderer }

    @TaskAction
    @Suppress("unused")
    fun exportFossDependencies() {
        val htmlRenderer = this.htmlRenderer ?: throw InvalidParameterException("You can't specify a different licenseReport.renderer")
        val source = File(htmlRenderer.fileDir, htmlRenderer.fileName)

        val outputFilePath = fossDependenciesExtension.outputFile ?: throw InvalidParameterException("You need to specify a fossDependencies.outputFile")
        val destination = File(outputFilePath)

        destination.parentFile.mkdirs()

        source.copyTo(destination, overwrite = true)
    }

}