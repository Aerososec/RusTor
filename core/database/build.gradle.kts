plugins{
    alias(libs.plugins.rustor.android.library)
    alias(libs.plugins.rustor.android.room)
    alias(libs.plugins.rustor.android.hilt)
}

android {
    namespace = "com.rustor.core.database"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies{
    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.bundles.testing.robolectric)
}