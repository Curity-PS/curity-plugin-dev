# Curity Plugin Dev

A Gradle plugin for Curity Identity Server plugin development that simplifies common development tasks.

## Features

- **createReleaseDir** – Assembles the plugin JAR and its runtime dependencies into a single folder under `build/release/<project-name>`, ready to be copied into the server's plugin directory.

- **createRelease** – Creates a zip file from the release directory, ready for distribution. The zip is placed in `build/distributions/plugin-artifacts.zip`.

- **deployToLocal** – Copies the release folder into a local Curity installation pointed to by the `IDSVR_HOME` environment variable.

- **integrationTest** – Runs integration tests (matched by a configurable pattern, default `*IntegrationSpec`) in a separate Test task. Requires `LICENSE_KEY` to be set and forwards it to the test JVM. The regular `test` task automatically excludes the same pattern so integration tests never run during a normal build.

## Installation

> **Note:** You must set up GitHub authentication before using this plugin. GitHub Packages requires authentication even for public repositories. See [GitHub Authentication](#github-authentication) section below for setup instructions.

### Add the plugin repository

In your project's `settings.gradle`, add the GitHub Packages repository:

```groovy
pluginManagement {
    repositories {
        maven {
            url 'https://maven.pkg.github.com/Curity-PS/curity-plugin-dev'
            credentials {
                username = System.getenv('GITHUB_ACTOR') ?: providers.gradleProperty('gpr.user')
                password = System.getenv('GITHUB_TOKEN') ?: providers.gradleProperty('gpr.token')
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
    id 'io.curity.gradle.curity-plugin-dev' version '0.1.0'
}
```

## Configuration

Configure the plugin in your `build.gradle`:

```groovy
curityPluginDev {
    integrationTestPattern = '*IntegrationSpec'
}
```

## GitHub Authentication

**Important:** GitHub Packages requires authentication even for public packages. Unlike Maven Central or npm registry, you must authenticate to download packages from GitHub Packages, regardless of repository visibility.

To use this plugin, you'll need to authenticate with GitHub Packages.

### Creating a GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Give it a name like "Gradle Package Access"
4. Select the `read:packages` scope
5. Click "Generate token"
6. Copy the token

### Option 1: Gradle Properties

Add to your `~/.gradle/gradle.properties`:

```properties
gpr.user=your-github-username
gpr.token=ghp_xxxxxxxxxxxxx
```

### Option 2: Environment Variables

This is mainly used by github actions, but you can also set these in your local environment:

```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=ghp_xxxxxxxxxxxxx
```


## Usage Examples

### Complete Project Setup

**settings.gradle:**

```groovy
pluginManagement {
    repositories {
        maven {
            url 'https://maven.pkg.github.com/Curity-PS/curity-plugin-dev'
            credentials {
                username = System.getenv('GITHUB_ACTOR') ?: project.findProperty('gpr.user')
                password = System.getenv('GITHUB_TOKEN') ?: project.findProperty('gpr.token')
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
# Set up your local Curity installation
export IDSVR_HOME=/path/to/curity/idsvr

# Deploy to local installation
./gradlew deployToLocal

# Restart the Curity Identity Server to load the plugin
```

**3. Running Integration Tests**

```bash
# Set your license key
export LICENSE_KEY=your-curity-license-key

# Run integration tests
./gradlew integrationTest
```

**4. Full Build & Test Cycle**

```bash
# Run all tests and create release
./gradlew clean build integrationTest createReleaseDir

# Or create a distributable zip file
./gradlew clean build integrationTest createRelease
```

### Environment Variables Reference

- `IDSVR_HOME` - Path to your Curity Identity Server installation (required for `deployToLocal`)
- `LICENSE_KEY` - Your Curity license key (required for `integrationTest`)
- `GITHUB_ACTOR` - Your GitHub username (required for accessing the plugin)
- `GITHUB_TOKEN` - Your GitHub personal access token with `read:packages` scope (required for accessing the plugin)

## Publishing Guide

This section is for maintainers who want to publish new versions of the plugin.

### Version Management with Conventional Commits

This project uses **Conventional Commits** and automatic versioning. You don't need to manually set version numbers.

#### How It Works

1. **Commit with conventional format**: Push commits to `main` using conventional commit messages
2. **Manual workflow trigger**: When ready to release, manually trigger the "Publish Plugin" workflow from GitHub Actions
3. **Automatic versioning**: The workflow analyzes commits and calculates a new version based on commit types:
   - `feat:` → Minor version bump (0.1.0 → 0.2.0)
   - `fix:` → Patch version bump (0.1.0 → 0.1.1)
   - `BREAKING CHANGE:` → Major version bump (0.1.0 → 1.0.0)
   - Other types (docs, chore, etc.) → No release
4. **Automatic release**: A GitHub release is created with changelog
5. **Automatic publish**: The plugin is published to GitHub Packages

#### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature (triggers minor version bump)
- `fix`: Bug fix (triggers patch version bump)
- `docs`: Documentation changes
- `chore`: Maintenance tasks
- `refactor`: Code refactoring
- `test`: Adding tests
- `ci`: CI/CD changes

**Examples:**

```bash
# Patch release (0.1.0 → 0.1.1)
git commit -m "fix: correct integration test task dependency"

# Minor release (0.1.0 → 0.2.0)
git commit -m "feat: add support for custom deployment paths"

# Major release (0.1.0 → 1.0.0)
git commit -m "feat: redesign plugin configuration

BREAKING CHANGE: The curityPlugin extension has been renamed to curityPluginDev"

# No release
git commit -m "docs: update README with usage examples"
```

### Initial Setup

1. **Create the GitHub repository**
   - Go to https://github.com/Curity-PS
   - Create a new repository named `curity-plugin-dev`
   - Initialize with this code

2. **Push the code**
   ```bash
   git init
   git add .
   git commit -m "feat: initial commit of Curity Plugin Dev"
   git branch -M main
   git remote add origin https://github.com/Curity-PS/curity-plugin-dev.git
   git push -u origin main
   ```

### Publishing Workflow

**Manual Trigger:**

1. Make your changes and commit with conventional format:
   ```bash
   git add .
   git commit -m "feat: add new deployment task"
   git push
   ```

2. Go to the GitHub Actions tab:
   - Navigate to https://github.com/Curity-PS/curity-plugin-dev/actions
   - Select "Publish Plugin" workflow
   - Click "Run workflow"
   - The workflow will:
     - Analyze commits since last release
     - Calculate the new version based on conventional commits
     - Create a git tag
     - Generate changelog
     - Create a GitHub release
     - Publish to GitHub Packages

**Note:** Releases are created manually to give you control over when versions are published. The workflow will skip release creation if there are no release-worthy commits (only docs, chore, etc.) since the last tag.

### Verifying Publication

After pushing, check:
- **Actions**: https://github.com/Curity-PS/curity-plugin-dev/actions
- **Releases**: https://github.com/Curity-PS/curity-plugin-dev/releases
- **Packages**: https://github.com/Curity-PS/curity-plugin-dev/packages

### Troubleshooting

**Authentication Issues**

If you get authentication errors:
- Verify your personal access token has `read:packages` (for users) or `write:packages` (for publishing)
- Check that `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables are set correctly
- Ensure your token hasn't expired

**Package Not Found**

If the package isn't visible after publishing:
- Check the Actions tab for any failed workflow runs
- Verify the package appears in the repository's Packages section
- Ensure the package visibility is set correctly (public or accessible to your organization)

### Best Practices

1. **Semantic Versioning**: Use semantic versioning (MAJOR.MINOR.PATCH)
2. **Release Notes**: Always include meaningful release notes
3. **Git Tags**: Keep git tags in sync with published versions
4. **Testing**: Test the plugin locally before publishing
5. **Documentation**: Update README.md when adding features

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

## FAQ

### Do I need authentication even if the repository is public?

**Yes.** GitHub Packages requires authentication for all package downloads, even from public repositories. This is a limitation of GitHub Packages and differs from other public package repositories like Maven Central or npm registry.

All users must:
1. Create a GitHub Personal Access Token with `read:packages` scope
2. Configure their credentials (see [GitHub Authentication](#github-authentication))

### Can I use this plugin without a GitHub account?

No. Since the plugin is hosted on GitHub Packages, all users need a GitHub account and must authenticate to download the plugin.

### What if my developers don't want to set up authentication?

If GitHub Packages authentication is a barrier for your team, consider these alternatives:
1. **Publish to Maven Central** - No authentication required for public artifacts
2. **Use JitPack** - Builds directly from GitHub releases with simpler authentication
3. **Host an internal Maven repository** - Use Nexus or Artifactory
4. **Distribute the JAR directly** - Share the built JAR file through other means

### Why use GitHub Packages then?

GitHub Packages is convenient when:
- Your organization already uses GitHub
- You want tight integration with your repository
- You need private package hosting
- You want automated publishing via GitHub Actions

## Testing Workflows Locally

You can test the GitHub Actions workflows locally using [act](https://github.com/nektos/act). See [LOCAL_TESTING.md](LOCAL_TESTING.md) for detailed instructions.

Quick start:
```bash
# List available workflows
act -l

# Test the publish workflow (without actually publishing)
./test-workflow.sh

# Test with a specific version
./test-workflow.sh --version 0.3.0
```

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

Copyright © Curity AB
