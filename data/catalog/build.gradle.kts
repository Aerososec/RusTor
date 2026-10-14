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

    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.bundles.testing.unit)
}