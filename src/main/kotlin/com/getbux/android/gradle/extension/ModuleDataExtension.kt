package com.getbux.android.gradle.extension

import com.github.jk1.license.ModuleData

val ModuleData.id get() = "$group:$name"