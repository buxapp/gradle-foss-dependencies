package com.getbux.android.gradle.render

data class ManualLicense(val license: FossLicense,
                         val inceptionYear: String? = null,
                         val authors: String? = null)