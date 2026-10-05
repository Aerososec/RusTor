import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.rustor.buildlogic.libs
import org.gradle.kotlin.dsl.configure
import androidx.room.gradle.RoomExtension


class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "implementation"(libs.findLibrary("androidx-room-runtime").get())
                "implementation"(libs.findLibrary("androidx-room-ktx").get())
                "ksp"(libs.findLibrary("androidx-room-compiler").get())
            }
        }
    }
}