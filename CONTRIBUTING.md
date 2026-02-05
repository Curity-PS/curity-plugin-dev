# Contributing to Curity Plugin Dev

Thank you for your interest in contributing to the Curity Plugin Dev Gradle plugin!

## Development Setup

### Prerequisites

- JDK 17 or later
- Git

### Clone and Build

```bash
git clone https://github.com/curity-ps/curity-plugin-dev.git
cd curity-plugin-dev
./gradlew build
```

## Making Changes

### Project Structure

```
curity-plugin-dev/
├── src/main/kotlin/io/curity/gradle/
│   ├── CurityPluginDevPlugin.kt      # Main plugin implementation
│   └── CurityPluginDevExtension.kt   # Plugin configuration DSL
├── build.gradle.kts                   # Build configuration
├── settings.gradle.kts                # Project settings
└── .github/workflows/                 # CI/CD workflows
```

### Testing Your Changes

1. **Build the plugin:**
   ```bash
   ./gradlew build
   ```

2. **Publish to Maven Local:**
   ```bash
   ./gradlew publishToMavenLocal
   ```

3. **Test in a sample project:**
   
   In your test project's `settings.gradle.kts`:
   ```kotlin
   pluginManagement {
       repositories {
           mavenLocal()  // Use the locally published version
           gradlePluginPortal()
       }
   }
   ```
   
   In `build.gradle.kts`:
   ```kotlin
   plugins {
       id("io.curity.gradle.curity-plugin-dev") version "0.1.0-SNAPSHOT"
   }
   ```

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions focused and concise

### Commit Messages

Use clear, descriptive commit messages:

```
Add feature to support custom deployment paths

- Added configuration option for custom deploy directory
- Updated documentation
- Added tests for new functionality
```

## Submitting Changes

1. **Fork the repository**

2. **Create a feature branch**
   ```bash
   git checkout -b feature/my-new-feature
   ```

3. **Make your changes and commit**
   ```bash
   git add .
   git commit -m "Add my new feature"
   ```

4. **Push to your fork**
   ```bash
   git push origin feature/my-new-feature
   ```

5. **Create a Pull Request**
   - Go to https://github.com/curity-ps/curity-plugin-dev
   - Click "New Pull Request"
   - Select your branch
   - Describe your changes
   - Submit the PR

## Pull Request Guidelines

- Ensure all tests pass
- Update documentation if needed
- Add tests for new features
- Keep PRs focused on a single concern
- Reference any related issues

## Release Process

Releases are automated through GitHub Actions:

1. Update version in `build.gradle.kts`
2. Commit and push the version change
3. Create a new release on GitHub with tag (e.g., `v0.2.0`)
4. GitHub Actions will automatically build and publish

## Questions?

If you have questions, please:
- Open an issue for bugs or feature requests
- Start a discussion for general questions

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.
