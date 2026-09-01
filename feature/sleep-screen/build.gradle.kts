plugins {
    alias(libs.plugins.fitdroid.android.feature)
}

android {
    namespace = "com.fitdroid.feature.sleep"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.sync)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.orbit.test)
}
