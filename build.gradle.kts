plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "io.curity.gradle"
version = project.findProperty("version") as String? ?: "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("curityPluginDev") {
            id = "io.curity.gradle.curity-plugin-dev"
            implementationClass = "io.curity.gradle.CurityPluginDevPlugin"
            displayName = "Curity Plugin Dev"
            description = "Gradle plugin for Curity Identity Server plugin development"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "io.curity.gradle"
            artifactId = "curity-plugin-dev"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/curity-ps/curity-plugin-dev-gradle-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
