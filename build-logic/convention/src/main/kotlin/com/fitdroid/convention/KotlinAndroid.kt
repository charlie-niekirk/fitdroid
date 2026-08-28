package com.fitdroid.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

internal fun ApplicationExtension.configureAndroidDefaults() {
    compileSdk {
        version = release(CompileSdk) { minorApiLevel = CompileSdkMinor }
    }
    defaultConfig.apply {
        minSdk = MinSdk
        targetSdk = TargetSdk
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

internal fun LibraryExtension.configureAndroidDefaults() {
    compileSdk {
        version = release(CompileSdk) { minorApiLevel = CompileSdkMinor }
    }
    defaultConfig.minSdk = MinSdk
    testOptions.targetSdk = TargetSdk
    lint.targetSdk = TargetSdk
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

internal fun Project.configureKotlinAndroid() {
    configureKotlin<KotlinAndroidProjectExtension>()
}

internal fun Project.configureKotlinJvm() {
    configureKotlin<KotlinJvmProjectExtension>()
}

private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() {
    extensions.configure<T> {
        val compilerOptions = when (this) {
            is KotlinAndroidProjectExtension -> compilerOptions
            is KotlinJvmProjectExtension -> compilerOptions
            else -> error("Unsupported Kotlin extension $this")
        }
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        jvmToolchain(17)
    }
}
