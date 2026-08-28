import com.fitdroid.convention.configureKotlinJvm
import com.fitdroid.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")
            configureKotlinJvm()
            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
