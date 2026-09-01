package com.fitdroid.convention

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

internal fun Project.configureSpotless() {
    apply(plugin = "com.diffplug.spotless")
    extensions.configure<SpotlessExtension> {
        val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
        val composeRulesVersion = libs.findVersion("composeRules").get().requiredVersion
        kotlin {
            target("src/**/*.kt")
            ktlint(ktlintVersion)
                .customRuleSets(
                    listOf("io.nlopez.compose.rules:ktlint:$composeRulesVersion"),
                )
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("xml") {
            target("src/**/*.xml")
            leadingTabsToSpaces(4)
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
