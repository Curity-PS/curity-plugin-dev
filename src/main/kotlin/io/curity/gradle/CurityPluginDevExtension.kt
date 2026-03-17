package io.curity.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Extension for configuring the Curity plugin tasks.
 *
 * Usage in build.gradle:
 * ```
 * curityPluginDev {
 *     integrationTestPattern = "*IntegrationSpec"
 * }
 * ```
 */
interface CurityPluginDevExtension {

    /**
     * The test name pattern used to identify integration tests.
     * Defaults to `*IntegrationSpec`.
     */
    val integrationTestPattern: Property<String>

    /**
     * The directory where the plugin release artifacts are assembled.
     * Defaults to `build/release/<project-name>`.
     *
     * This is useful for referencing the release directory in tests, e.g.:
     * ```
     * def releaseDir = project.curityPluginDev.releaseDir.get().asFile.absolutePath
     * ```
     */
    val releaseDir: DirectoryProperty
}
