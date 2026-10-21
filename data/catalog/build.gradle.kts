plugins {
    alias(libs.plugins.rustor.android.library)
    alias(libs.plugins.rustor.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rustor.data.catalog"
}

dependencies{
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.testing.unit)
}