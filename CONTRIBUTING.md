# Contributing to NeuroPath

Thank you for your interest in contributing to NeuroPath! This document outlines the process for contributing to this project.

## Code of Conduct

This project adheres to a code of conduct adapted from the [Contributor Covenant](https://www.contributor-covenant.org/). By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report, please check the [existing issues](https://github.com/fourgeailabs/NeuroPath/issues) to avoid duplicates.

When creating a bug report, include:

- **Clear title** describing the issue
- **Steps to reproduce** the problem
- **Expected behavior** vs **actual behavior**
- **Device/OS information** (Android version, device model, API level)
- **Screenshots/logs** if applicable
- **App version** (from Settings or build)

### Suggesting Enhancements

Enhancement suggestions are welcome! Please include:

- **Use case** - What problem does this solve?
- **Proposed solution** - How should it work?
- **Alternatives considered** - Other approaches you've thought of
- **Impact** - Who benefits and how?

### Pull Requests

1. **Fork** the repository
2. **Create a branch** from `main` with a descriptive name:
   - `feature/add-new-theme` for new features
   - `fix/crash-on-startup` for bug fixes
   - `docs/update-readme` for documentation
3. **Make your changes** following the coding standards below
4. **Test your changes** locally:
   - Run `./gradlew.bat assembleDebug` (Windows) or `./gradlew assembleDebug` (Linux/macOS)
   - Run `./gradlew.bat testDebugUnitTest` to execute unit tests
5. **Submit a Pull Request** with:
   - Clear title and description
   - Reference to related issues (e.g., "Fixes #123")
   - Screenshots for UI changes

## Development Setup

See the [Getting Started section in README.md](README.md#%EF%B8%8F-getting-started--development-setup) for detailed setup instructions.

### Prerequisites

- JDK 21
- Android SDK (API 36)
- Gradle 9.3.1 (via wrapper)

### Local Build

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/NeuroPath.git
cd NeuroPath

# Set up debug keystore
cp debug.keystore_test debug.keystore

# Configure secrets
cp .env.example .env
# Edit .env with your GEMINI_API_KEY

# Build and test
./gradlew.bat assembleDebug
./gradlew.bat testDebugUnitTest
```

## Coding Standards

### Kotlin

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `ktlint` formatting (enforced by CI)
- Prefer `val` over `var`
- Use meaningful names
- Write documentation for public APIs

### Jetpack Compose

- Follow [Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- Use `@Preview` for UI components
- Keep composables small and focused
- Use `remember` and `derivedStateOf` appropriately

### Architecture

- Follow MVVM with ViewModels and StateFlow
- Repository pattern for data layer
- Dependency injection via constructor
- Single source of truth for state

### Testing

- Write unit tests for business logic (`test/` directory)
- Write UI tests for critical flows (`androidTest/` directory)
- Aim for meaningful test coverage
- Test names should describe behavior: `shouldReturnCorrectResultWhenInputIsValid()`

### Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

Types:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `refactor` - Code restructuring
- `test` - Tests
- `chore` - Maintenance
- `perf` - Performance

Examples:
```
feat(theme): add new dinosaur theme variant
fix(chat): resolve crash when sending empty message
docs(readme): update build instructions for Linux
```

## Code Review Process

1. All PRs require at least one review from a maintainer
2. CI checks must pass (build, tests, lint)
3. Address review feedback promptly
4. Maintainers will merge after approval

## Release Process

Releases are created by maintainers via Git tags:

1. Update version in `app/build.gradle.kts`
2. Update CHANGELOG.md
3. Create tag: `git tag v2.0.1`
4. Push tag: `git push origin v2.0.1`
5. GitHub Actions builds release APK and creates GitHub Release

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).