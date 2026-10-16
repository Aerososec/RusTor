plugins{
    alias(libs.plugins.rustor.jvm.library)
}

dependencies{
    implementation(project(":core:model"))
    implementation(libs.bundles.coroutines)
}