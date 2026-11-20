import com.android.build.api.dsl.LibraryExtension
import com.rustor.buildlogic.configureKotlinAndroid
import com.rustor.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("rustor.detekt")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.testInstrumentationRunner =
                    "androidx.test.runner.AndroidJUnitRunner"
            }

            dependencies.add(
                "androidTestImplementation",
                libs.findLibrary("androidx-test-runner").get(),
            )
        }
    }
}