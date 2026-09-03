plugins {
    alias(libs.plugins.fitdroid.android.feature)
}

android {
    namespace = "com.fitdroid.feature.reports"
}

dependencies {
    implementation(projects.core.database)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.orbit.test)
}
