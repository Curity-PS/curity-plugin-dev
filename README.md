# Curity Plugin Dev

A Gradle plugin for Curity Identity Server plugin development that simplifies common development tasks.

## Features

- **createReleaseDir** – Assembles the plugin JAR and its runtime dependencies into a single folder under `build/release/<project-name>`, ready to be copied into the server's plugin directory.

- **createRelease** – Creates a zip file from the release directory, ready for distribution. The zip is placed in `build/distributions/plugin-artifacts.zip`.

- **deployToLocal** – Copies the release folder into a local Curity installation pointed to by the `IDSVR_HOME` environment variable.

- **integrationTest** – Runs integration tests (matched by a configurable pattern, default `*IntegrationSpec`) in a separate Test task. Requires `LICENSE_KEY` to be set and forwards it to the test JVM. The regular `test` task automatically excludes the same pattern so integration tests never run during a normal build.

## Installation

> **Note:** GitHub Packages requires authentication even for public repositories. You need to configure your GitHub credentials in `~/.gradle/gradle.properties` before using this plugin. See [GitHub Authentication](#github-authentication) for setup instructions.

### Add the plugin repository

In your project's `settings.gradle`, add the GitHub Packages repository:

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url 'https://maven.pkg.github.com/Curity-PS/curity-plugin-dev'
            credentials {
                username = System.getenv('GITHUB_ACTOR') ?: providers.gradleProperty('gpr.user').getOrNull()
                password = System.getenv('GITHUB_TOKEN') ?: providers.gradleProperty('gpr.token').getOrNull()
            }
        }
        gradlePluginPortal()
    }
}
```

### Apply the plugin

In your project's `build.gradle`:

```groovy
plugins {
    id 'io.curity.gradle.curity-plugin-dev' version 'x.y.z'
}
```

## Configuration

Optionally configure the plugin in your `build.gradle`:

```groovy
curityPluginDev {
    integrationTestPattern = '*IntegrationSpec'
}
```
## Environment Variables Reference

- `IDSVR_HOME` - Path to your Curity Identity Server installation (required for `deployToLocal`)
- `LICENSE_KEY` - Your Curity license key (required for `integrationTest`)

## Environment Variable Resolution

The plugin resolves environment variables in the following order:

1. **System environment** – real environment variables (e.g. `export IDSVR_HOME=...`)
2. **Project `.env` file** – a `.env` file in the project root
3. **Gradle properties** – from `~/.gradle/gradle.properties` (or project-level `gradle.properties`)

Values found earlier in the lookup order take precedence. This means a project-level `.env` overrides Gradle properties, and a real environment variable overrides both.

Gradle properties are convenient for values that are the same across all your Curity plugin projects. Add them to `~/.gradle/gradle.properties`:

```properties
curity.licenseKey=your-curity-license-key
curity.idsvrHome=/path/to/curity/idsvr
```

| Environment Variable | Gradle Property      |
|----------------------|----------------------|
| `LICENSE_KEY`        | `curity.licenseKey`  |
| `IDSVR_HOME`        | `curity.idsvrHome`   |

You can override specific values per project using a `.env` file or environment variables.

| Property                 | Type               | Default                             | Description                                      |
|--------------------------|--------------------|-------------------------------------|--------------------------------------------------|
| `integrationTestPattern` | `String`           | `*IntegrationSpec`                  | Pattern used to identify integration test classes |
| `releaseDir`             | `DirectoryProperty`| `build/release/<project-name>`      | Directory where release artifacts are assembled   |

## GitHub Authentication

GitHub Packages requires authentication even for public packages. To download this plugin, add your GitHub credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_TOKEN
```

To create a token:

1. Go to https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Give it a name like "Gradle Package Access"
4. Select the `read:packages` scope
5. Click "Generate token"

## Usage Examples

### Complete Project Setup

**settings.gradle:**

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url 'https://maven.pkg.github.com/Curity-PS/curity-plugin-dev'
            credentials {
                username = System.getenv('GITHUB_ACTOR') ?: providers.gradleProperty('gpr.user').getOrNull()
                password = System.getenv('GITHUB_TOKEN') ?: providers.gradleProperty('gpr.token').getOrNull()
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = 'my-curity-plugin'
```

**build.gradle:**

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.9.22'
    id 'io.curity.gradle.curity-plugin-dev' version '0.1.0'
}

group = 'com.example'
version = '1.0.0'

repositories {
    mavenCentral()
    maven {
        url 'https://repo.curity.io/releases'
    }
}

dependencies {
    implementation 'org.jetbrains.kotlin:kotlin-stdlib'
    
    // Curity SDK
    compileOnly 'se.curity.identityserver:identityserver.sdk:8.6.0'
    
    // Testing
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testImplementation 'org.testcontainers:testcontainers:1.19.0'
}

// Configure the plugin
curityPluginDev {
    integrationTestPattern = '*IntegrationTest'
}

test {
    useJUnitPlatform()
}
```

### Development Workflow

**1. Development**

```bash
# Build your plugin
./gradlew build

# Create release directory
./gradlew createReleaseDir

# The plugin files are now in build/release/<project-name>/

# Create a distributable zip file
./gradlew createRelease

# The zip file is now in build/distributions/plugin-artifacts.zip
```

**2. Testing Locally**

```bash
# Set up your local Curity installation (or configure IDSVR_HOME before)
export IDSVR_HOME=/path/to/curity/idsvr

# Deploy to local installation
./gradlew deployToLocal

# Restart the Curity Identity Server to load the plugin
```

**3. Running Integration Tests**

```bash
# Set your license key (or configure LICENSE_KEY before)
export LICENSE_KEY=your-curity-license-key

# Run integration tests
./gradlew integrationTest
```

**4. Referencing the Release Directory in Tests**

The `releaseDir` property is useful when your tests need to know the path to the assembled plugin artifacts, e.g. when mounting them into a Testcontainer. Pass it as a system property to avoid hardcoding the path:

```groovy
// build.gradle
integrationTest {
    systemProperty 'releaseDir', curityPluginDev.releaseDir.get().asFile.absolutePath
}
```

Then in a Spock test:

```groovy
class MyPluginSpec extends Specification {
    def releaseDir = System.getProperty('releaseDir')

    def "plugin artifacts are available"() {
        expect:
        new File(releaseDir).exists()
    }
}
```

**5. Full Build & Test Cycle**

```bash
# Run all tests and create release
./gradlew clean build integrationTest createReleaseDir

# Or create a distributable zip file
./gradlew clean build integrationTest createRelease
```

## Development

### Building the plugin

```bash
./gradlew build
```

### Testing locally

```bash
./gradlew publishToMavenLocal
```

Then use `mavenLocal()` in your test project's repository configuration.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

Copyright © Curity AB