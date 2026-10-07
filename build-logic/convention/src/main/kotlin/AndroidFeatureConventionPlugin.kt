import com.rustor.buildlogic.configureAndroidCompose
import com.rustor.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import com.android.build.api.dsl.LibraryExtension

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("rustor.android.library")
            pluginManager.apply("rustor.android.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
            }

            dependencies {
                "implementation"(libs.findBundle("lifecycle").get())
                "implementation"(libs.findBundle("coroutines").get())
                "implementation"(libs.findLibrary("hilt-lifecycle-viewmodel-compose").get())

                "testImplementation"(libs.findBundle("testing-unit").get())
                "androidTestImplementation"(libs.findBundle("testing-compose").get())
            }
        }
    }
}