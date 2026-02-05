package io.curity.gradle

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
}
