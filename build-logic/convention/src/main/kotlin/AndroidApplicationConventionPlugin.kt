import com.android.build.api.dsl.ApplicationExtension
import com.fitdroid.convention.configureAndroidDefaults
import com.fitdroid.convention.configureDetekt
import com.fitdroid.convention.configureKotlinAndroid
import com.fitdroid.convention.configureSpotless
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            extensions.configure<ApplicationExtension> {
                configureAndroidDefaults()
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
                // GitHub Actions sets CI=true. Debug-sign release APKs there so they can be
                // installed on a device or emulator without a production keystore.
                val useCiReleaseSigning = providers.environmentVariable("CI")
                    .map { it.equals("true", ignoreCase = true) }
                    .orElse(false)
                    .get()
                if (useCiReleaseSigning) {
                    buildTypes.named("release") {
                        signingConfig = signingConfigs.getByName("debug")
                    }
                }
            }
            configureKotlinAndroid()
            configureSpotless()
            configureDetekt()
        }
    }
}
