import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

// Keep the checked-in Gemini client compatible with the currently supported API models.
// This is intentionally a build-time guard so stale model IDs cannot ship in an APK.
val normalizeGeminiClientModels = tasks.register("normalizeGeminiClientModels") {
  doLast {
    val source = file("src/main/java/com/example/network/GeminiClient.kt")
    if (!source.exists()) return@doLast

    var text = source.readText()
    val replacements = linkedMapOf(
      "gemini-1.5-flash" to "gemini-3.7-flash",
      "gemini-1.5-pro" to "gemini-2.5-pro",
      "gemini-2.0-flash" to "gemini-3.7-flash",
      "Gemini 1.5 Flash" to "Gemini 3.7 Flash",
      "Gemini 1.5 Pro" to "Gemini 2.5 Pro",
      "gemma-2b-it-gpu-int4" to "gemma-2-2b-it-Q4_K_M.gguf",
      "Gemma 2B Local" to "Gemma 2 2B Local",
      "2020+ Device Compatible (INT4 GPU/CPU)" to "GGUF • CPU/NEON local inference",
      "Executing Gemini 1.5 (" to "Executing Gemini ("
    )
    replacements.forEach { (old, new) -> text = text.replace(old, new) }
    text = text.replace(
      " with device GPU hardware acceleration (Vulkan/OpenCL delegate) active.",
      "."
    )
    source.writeText(text)
  }
}

tasks.configureEach {
  if (name.contains("Kotlin", ignoreCase = true) && name.contains("Compile", ignoreCase = true)) {
    dependsOn(normalizeGeminiClientModels)
  }
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.fourgeailabs.neuropath"
    minSdk = 24
    targetSdk = 36
    versionCode = 36
    versionName = "1.25.00"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    ndk { abiFilters += listOf("arm64-v8a") }
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
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

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.llama.android)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
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
