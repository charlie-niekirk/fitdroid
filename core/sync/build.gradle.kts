plugins {
    alias(libs.plugins.fitdroid.android.library)
    alias(libs.plugins.fitdroid.metro)
}

android {
    namespace = "com.fitdroid.core.sync"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    implementation(projects.core.health)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.scoring)
    implementation(projects.core.ui)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
