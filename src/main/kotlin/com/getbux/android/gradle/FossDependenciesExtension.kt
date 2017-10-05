package com.getbux.android.gradle

import com.getbux.android.gradle.render.FossLicense
import com.getbux.android.gradle.render.ManualLicense

@Suppress("unused")
open class FossDependenciesExtension {
    var outputFile: String? = null
    fun outputFile(value: String) { outputFile = value }

    var htmlTitle: String? = null
    fun htmlTitle(value: String) { htmlTitle = value }

    open class DependenciesExtension {

        val APACHE_2 = FossLicense.APACHE_2
        val BSD_2 = FossLicense.BSD2
        val BSD_3 = FossLicense.BSD3
        val JDOM = FossLicense.JDOM
        val MIT = FossLicense.MIT
        val EPL = FossLicense.EPL

        var ignoredDependencies = mutableListOf<Regex>()
        var manualLicenses = mutableMapOf<Regex, ManualLicense>()

        fun ignore(dependency: String) {
            ignoredDependencies.add(transformStringToRegEx(dependency))
        }

        @JvmOverloads
        fun manual(dependency: String, license: FossLicense, authors: String? = null, inceptionYear: String? = null) {
            manualLicenses[transformStringToRegEx(dependency)] = ManualLicense(license, inceptionYear, authors)
        }

        private fun transformStringToRegEx(string: String) = Regex(string.replace("*", ".*"))
    }
}