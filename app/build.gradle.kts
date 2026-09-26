import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

android {
  namespace = "com.fourgeailabs.neuropath"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.fourgeailabs.neuropath"
    minSdk = 24
    targetSdk = 36
    versionCode = 45
    versionName = "2.05.00"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    ndk { abiFilters += listOf("arm64-v8a") }
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      val uploadKeyFile = file(keystorePath)
      // NB: this block runs at configuration time for EVERY build (even assembleDebug), so the
      // missing-keystore failure must only trigger when a release task was actually requested.
      val buildingRelease = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
      if (uploadKeyFile.exists()) {
        storeFile = uploadKeyFile
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      } else if (buildingRelease) {
        // Fail clearly: a release build must never be silently signed with a debug key.
        // Provide the upload keystore via KEYSTORE_PATH (plus STORE_PASSWORD / KEY_PASSWORD),
        // or place it at ${rootDir}/my-upload-key.jks.
        throw GradleException(
          "Release signing keystore not found at '$keystorePath'. " +
          "Set the KEYSTORE_PATH environment variable (with STORE_PASSWORD and KEY_PASSWORD) " +
          "or place your upload keystore at <project>/my-upload-key.jks. " +
          "Refusing to sign a release build with a debug key."
        )
      } else {
        logger.warn("No upload keystore at '$keystorePath'; release signing left unconfigured (debug build).")
      }
    }
    create("debugConfig") {
      val debugKeystoreFile = file("${rootDir}/debug.keystore")
      val debugKeystoreBase64File = file("${rootDir}/debug.keystore.base64")
      if (!debugKeystoreFile.exists() && debugKeystoreBase64File.exists()) {
        runCatching { debugKeystoreFile.writeBytes(Base64.getDecoder().decode(debugKeystoreBase64File.readText().trim())) }
      }
      storeFile = debugKeystoreFile
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      if (file("${rootDir}/debug.keystore").exists()) signingConfig = signingConfigs.getByName("debugConfig")
      // Debug also ships x86_64 native libs (litertlm provides them) so instrumented
      // tests can run on the x86_64 CI emulator. Release stays arm64-v8a-only.
      ndk { abiFilters += listOf("x86_64") }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true; buildConfig = true }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo { includeInApk = false; includeInBundle = true }
}

// Detekt configuration
detekt {
  buildUponDefaultConfig = true
  config.setFrom(files("$rootDir/detekt.yml"))
  baseline = file("$rootDir/detekt-baseline.xml")
  ignoreFailures = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
  reports {
    html.required.set(true)
    xml.required.set(true)
    txt.required.set(true)
    sarif.required.set(true)
  }
}

// Ktlint configuration
ktlint {
  android.set(true)
  outputToConsole.set(true)
  ignoreFailures.set(true)
  reporters {
    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.security.crypto)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.llama.android)
  implementation("com.google.ai.edge.litertlm:litertlm-android:0.16.1")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation("org.jetbrains.kotlin:kotlin-test:2.2.10")
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
