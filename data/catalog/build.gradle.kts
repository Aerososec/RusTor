plugins {
    alias(libs.plugins.rustor.android.library)
    alias(libs.plugins.rustor.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rustor.data.catalog"
}

dependencies{
    implementation(libs.kotlinx.serialization.json)
}