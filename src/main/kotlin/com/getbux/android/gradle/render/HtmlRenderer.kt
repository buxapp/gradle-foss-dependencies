package com.getbux.android.gradle.render

import com.getbux.android.gradle.extension.id
import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.logging.Logging
import java.io.File
import java.security.InvalidParameterException

class HtmlRenderer(val fileName: String = "foss_dependencies.html") : ReportRenderer {

    var title: String? = null

    lateinit var fileDir: String
    lateinit var ignoredDependenciesRegExes: List<Regex>
    lateinit var manualLicenses: Map<Regex, ManualLicense>

    override fun render(data: ProjectData) {
        val modules = data.allDependencies.toMutableSet()
        removeIgnoredModules(modules)

        val licensedModules = resolveLicenses(modules).distinct()

        val output = File(fileDir, fileName).apply {
            createNewFile()
        }

        output.writeText(HTML_START.format(title ?: DEFAULT_TITLE))

        val groupedModules = licensedModules.groupBy { it.license }

        groupedModules.keys.sortedWith(compareBy(FossLicense::name)).forEach { license ->
            val moduleLicenses = groupedModules[license]!!.sortedWith(compareBy(ModuleLicense::name))
            output.appendText(renderLicense(license, moduleLicenses))
        }

        output.appendText(HTML_END)
    }

    private fun removeIgnoredModules(modules: MutableSet<ModuleData>) {
        modules.removeIf { module ->
            ignoredDependenciesRegExes.find { it.matches(module.id) } != null
        }
    }

    private fun resolveLicenses(modules: Set<ModuleData>): List<ModuleLicense> {
        return modules.map { module ->
            LOGGER.info("Processing module ${module.id}")

            val name = extractName(module)

            for ((regEx, manualLicense) in manualLicenses) {
                if (regEx.matches(module.id)) {
                    return@map ModuleLicense(name, manualLicense.license, manualLicense.inceptionYear, manualLicense.authors)
                }
            }

            ModuleLicense(name, extractLicense(module), extractInceptionYear(module), extractAuthors(module))
        }
    }

    private fun renderLicense(license: FossLicense, moduleLicenses: List<ModuleLicense>): String {
        return """
               <h3>
                   ${license.names.first()}
               </h3>
               <ul>
                   ${moduleLicenses.joinToString("\n") { renderModule(it) }}
               </ul>
               <pre>${license.notice}</pre>
               """
    }

    private fun renderModule(module: ModuleLicense): String {
        return StringBuilder().apply {
            append("<li>")
            append("<b>${module.name}</b>")

            if (module.authors != null) {
                append("<br/>")
                append("Copyright")

                if (module.inceptionYear != null) {
                    append(" ${module.inceptionYear}")
                }

                append(" ${module.authors}")
            }
            append("</li>")
        }.toString()
    }

    private fun extractName(module: ModuleData): String {
        module.manifests.map { it.name }.find { !it.isNullOrEmpty() }?.let { return it }
        module.poms.map { it.name }.find { !it.isNullOrEmpty() }?.let { return it }
        return module.name
    }

    private fun extractLicense(module: ModuleData): FossLicense {
        val moduleLicenses = mutableListOf<String>()
        module.manifests.mapNotNullTo(moduleLicenses) { it.license }
        module.poms.flatMap { it.licenses }.mapNotNullTo(moduleLicenses) { it.name }

        val fossLicense = FossLicense.values().find { license ->
            moduleLicenses.find { moduleLicense -> license.names.contains(moduleLicense) } != null
        }

        if (fossLicense == null) {
            LOGGER.error("Couldn't find a ${FossLicense::class.java.simpleName} for module ${module.id}. Candidates were:\n$moduleLicenses")
        }

        return fossLicense ?: throw InvalidParameterException("No license found for module ${module.id}, please specify one manually")
    }

    private fun extractInceptionYear(module: ModuleData): String? {
        return module.poms.map { it.inceptionYear }.find { !it.isNullOrEmpty() }
    }

    private fun extractAuthors(module: ModuleData): String? {
        val vendors = module.manifests.mapNotNull { it.vendor }
        if (vendors.isNotEmpty()) {
            LOGGER.info("Found ${vendors.size} vendors")
            return vendors.joinToString(", ")
        }

        val organizations = module.poms.mapNotNull { it.organization }
        if (organizations.isNotEmpty()) {
            LOGGER.info("Found ${organizations.size} organizations")
            return organizations.joinToString(", ") { it.name }
        }

        val developers = module.poms.flatMap { it.developers }.filterNotNull()
        if (developers.isNotEmpty()) {
            LOGGER.info("Found ${developers.size} developers")
            return developers.joinToString(", ") { it.name }
        }

        LOGGER.info("Couldn't find author(s) for module ${module.id}")
        return null
    }

    companion object {
        private val LOGGER = Logging.getLogger(HtmlRenderer::class.java)

        private const val DEFAULT_TITLE = "Open Source Libraries"

        private const val HTML_START = """
            <html>
            <head>
                <title>%s</title>
                <style>
                    body {
                        font-family: sans-serif;
                    }
                    h3 {
                        padding: 0.5em 1em 0 1em;
                    }
                    pre {
                        background-color: #eeeeee;
                        padding: 1em;
                        white-space: pre-wrap;
                    }
                </style>
            </head>
            <body>
            """

        private const val HTML_END = """
            </body>
            </html>
            """
    }
}