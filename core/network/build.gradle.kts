plugins {
    alias(libs.plugins.fitdroid.android.library)
    alias(libs.plugins.fitdroid.metro)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.fitdroid.core.network"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    implementation(projects.core.auth)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
