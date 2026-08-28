import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.fitdroid.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            extensions.findByType<ApplicationExtension>()?.let(::configureAndroidCompose)
                ?: extensions.findByType<LibraryExtension>()?.let(::configureAndroidCompose)
                ?: error(
                    "Apply fitdroid.android.application or fitdroid.android.library before fitdroid.android.compose.",
                )
        }
    }
}
