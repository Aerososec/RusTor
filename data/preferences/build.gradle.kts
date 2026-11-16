plugins {
    alias(libs.plugins.rustor.android.library)
    alias(libs.plugins.rustor.android.hilt)
}

android {
    namespace = "com.rustor.data.preferences"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.bundles.testing.robolectric)
}
