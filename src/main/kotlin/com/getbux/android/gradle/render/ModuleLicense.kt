package com.getbux.android.gradle.render

data class ModuleLicense(val name: String,
                         val license: FossLicense,
                         val inceptionYear: String? = null,
                         val authors: String? = null)