plugins {
    alias(libs.plugins.rustor.android.feature)
}

android {
    namespace = "com.rustor.feature.onboarding"
}

dependencies{
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
}