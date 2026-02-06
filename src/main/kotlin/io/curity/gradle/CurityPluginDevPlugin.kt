package io.curity.gradle

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test

/**
 * Gradle plugin that registers common tasks for Curity Identity Server plugin projects:
 *
 * - **createReleaseDir** – assembles the plugin JAR and its runtime dependencies into a
 *   single folder under `build/release/<project-name>`, ready to be copied into the server's
 *   plugin directory.
 *
 * - **deployToLocal** – copies the release folder into a local Curity installation
 *   pointed to by the `IDSVR_HOME` environment variable.
 *
 * - **integrationTest** – runs integration tests (matched by a configurable pattern,
 *   default `*IntegrationSpec`) in a separate Test task.  Requires `LICENSE_KEY` to be
 *   set and forwards it to the test JVM.  The regular `test` task automatically excludes
 *   the same pattern so integration tests never run during a normal build.
 */
class CurityPluginDevPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create(
            "curityPluginDev",
            CurityPluginDevExtension::class.java
        )
        extension.integrationTestPattern.convention("*IntegrationSpec")

        project.afterEvaluate {
            configureTestExclusion(project, extension)
            registerCreateReleaseDir(project)
            registerDeployToLocal(project)
            registerIntegrationTest(project, extension)
        }
    }

    // ------------------------------------------------------------------ tasks

    private fun registerCreateReleaseDir(project: Project) {
        val releaseDir = project.layout.buildDirectory.dir("release/${project.name}")

        project.tasks.register("createReleaseDir", Sync::class.java, Action<Sync> {
            group = "build"
            description = "Assembles the plugin JAR and runtime dependencies into a release folder"
            dependsOn(project.tasks.named("jar"))

            into(releaseDir)
            from(project.tasks.named("jar"))
            from(project.configurations.named("runtimeClasspath"))

            doLast(Action<Task> {
                project.logger.lifecycle("Plugin prepared for deployment at: $releaseDir")
                project.logger.lifecycle("Copy the release folder to \$IDSVR_HOME/usr/share/plugins/")
            })
        })
    }

    private fun registerDeployToLocal(project: Project) {
        project.tasks.register("deployToLocal", Sync::class.java, Action<Sync> {
            group = "deployment"
            description = "Deploys the plugin to a local Curity Identity Server"
            dependsOn(project.tasks.named("createReleaseDir"))

            doFirst(Action<Task> {
                val idsvrHome = System.getenv("IDSVR_HOME")
                if (idsvrHome.isNullOrBlank()) {
                    throw GradleException(
                        "IDSVR_HOME environment variable is not set.\n" +
                            "Please set it to your Curity Identity Server installation directory:\n" +
                            "  export IDSVR_HOME=/path/to/idsvr"
                    )
                }
            })

            val idsvrHome = System.getenv("IDSVR_HOME")
            if (!idsvrHome.isNullOrBlank()) {
                from(project.tasks.named("createReleaseDir"))
                into(project.file("$idsvrHome/usr/share/plugins/${project.name}"))
            }

            doLast(Action<Task> {
                val home = System.getenv("IDSVR_HOME")
                project.logger.lifecycle("Plugin installed to: $home/usr/share/plugins/${project.name}")
                project.logger.lifecycle("Restart the Curity Identity Server to load the plugin.")
            })
        })
    }

    private fun registerIntegrationTest(project: Project, extension: CurityPluginDevExtension) {
        val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)
        val testSourceSet = javaExt.sourceSets.getByName("test")

        project.tasks.register("integrationTest", Test::class.java, Action<Test> {
            group = "verification"
            description = "Runs integration tests with Testcontainers against Curity Identity Server"

            useJUnitPlatform()

            val pattern = extension.integrationTestPattern.get()
            filter.includeTestsMatching(pattern)

            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath

            dependsOn(project.tasks.named("testClasses"), project.tasks.named("createReleaseDir"))
            shouldRunAfter(project.tasks.named("test"))

            doFirst(Action<Task> {
                val licenseKey = System.getenv("LICENSE_KEY")
                if (licenseKey.isNullOrBlank()) {
                    throw GradleException(
                        "LICENSE_KEY environment variable is not set.\n" +
                            "A valid Curity license key is required to run integration tests.\n" +
                            "  export LICENSE_KEY=<your-license-key>"
                    )
                }
            })

            environment("LICENSE_KEY", System.getenv("LICENSE_KEY") ?: "")

            reports.html.outputLocation.set(
                project.layout.buildDirectory.dir("reports/tests/integrationTest")
            )
            reports.junitXml.outputLocation.set(
                project.layout.buildDirectory.dir("test-results/integrationTest")
            )
        })
    }

    // --------------------------------------------------------------- helpers

    private fun configureTestExclusion(project: Project, extension: CurityPluginDevExtension) {
        project.tasks.named("test", Test::class.java, Action<Test> {
            filter.excludeTestsMatching(extension.integrationTestPattern.get())
        })
    }
}
