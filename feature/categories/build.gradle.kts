plugins {
    alias(libs.plugins.rustor.android.feature)
}

android {
    namespace = "com.rustor.feature.categories"
}

dependencies{
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.activity.compose)
}