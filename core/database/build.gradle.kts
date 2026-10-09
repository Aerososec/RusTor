plugins{
    alias(libs.plugins.rustor.android.library)
    alias(libs.plugins.rustor.android.room)
    alias(libs.plugins.rustor.android.hilt)
}

android {
    namespace = "com.rustor.core.database"
}