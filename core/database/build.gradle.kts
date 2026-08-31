plugins {
    alias(libs.plugins.fitdroid.android.library)
    alias(libs.plugins.fitdroid.room)
    alias(libs.plugins.fitdroid.metro)
}

android {
    namespace = "com.fitdroid.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
