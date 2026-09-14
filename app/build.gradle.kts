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

// Keep checked-in legacy integrations from shipping claims/model IDs that no longer match reality.
// The source files are normalized before Kotlin compilation while the large legacy client is migrated.
val normalizeLegacyIntegrations = tasks.register("normalizeLegacyIntegrations") {
  doLast {
    val gemini = file("src/main/java/com/example/network/GeminiClient.kt")
    if (gemini.exists()) {
      var text = gemini.readText()
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
      text = text.replace(" with device GPU hardware acceleration (Vulkan/OpenCL delegate) active.", ".")

      val voiceStartMarker = "    /**\n     * Voice Conversations (Live API mode) with full curriculum access.\n     */"
      val voiceEndMarker = "    /**\n     * Generate Music for Soundscapes"
      val voiceStart = text.indexOf(voiceStartMarker)
      val voiceEnd = text.indexOf(voiceEndMarker, voiceStart)
      if (voiceStart >= 0 && voiceEnd > voiceStart) {
        val liveMethod = """    /** Real Gemini Live API WebSocket turn. */
    suspend fun generateLiveVoiceConversationTurn(
        userVoiceAudio: ByteArray?,
        userText: String?,
        conversationHistory: List<Pair<String, String>>,
        systemPrompt: String,
        curriculumContext: String,
        schoolDistrict: String,
        stateOrProvince: String,
        country: String,
        standardTitle: String,
        languageCode: String,
        customApiKey: String = ""
    ): LiveVoiceTurnResult = GeminiLiveApiClient.generateTurn(
        apiKey = getApiKey(customApiKey),
        userVoiceAudio = userVoiceAudio,
        userText = userText,
        conversationHistory = conversationHistory,
        systemPrompt = systemPrompt,
        curriculumContext = curriculumContext,
        schoolDistrict = schoolDistrict,
        stateOrProvince = stateOrProvince,
        country = country,
        standardTitle = standardTitle,
        languageCode = languageCode
    )

"""
        text = text.substring(0, voiceStart) + liveMethod + text.substring(voiceEnd)
      }
      gemini.writeText(text)
    }

    val viewModel = file("src/main/java/com/example/ui/NeuroPathViewModel.kt")
    if (viewModel.exists()) {
      var text = viewModel.readText()
      text = text.replace(
        "lyriaMusicPlayer.playAudioFromBase64(result.audioBase64, \"Live Buddy Voice\", loop = false)",
        "lyriaMusicPlayer.playPcm16FromBase64(result.audioBase64, \"Live Buddy Voice\", loop = false)"
      )

      // Inject the learner fingerprint into every main Learning Buddy chat request.
      val chatProfileAnchor = "val currentSubject = _selectedSubjectTag.value\n        val profile = _currentProfile.value\n        val activeSession = _currentSessionId.value"
      if (text.contains(chatProfileAnchor) && !text.contains("val personalizationProfile = com.example.learning.LearnerPersonalizationEngine.buildPrompt")) {
        text = text.replace(
          chatProfileAnchor,
          "val currentSubject = _selectedSubjectTag.value\n        val profile = _currentProfile.value\n        val personalizationProfile = com.example.learning.LearnerPersonalizationEngine.buildPrompt(getApplication(), profile, currentSubject.id)\n        val activeSession = _currentSessionId.value"
        )
      }
      text = text.replace(
        "Learner profile accommodation considerations: ${profile.neurodivergentTypesCsv}.",
        "Learner profile accommodation considerations: ${profile.neurodivergentTypesCsv}.\n                        $personalizationProfile"
      )
      text = text.replace(
        "val basePrompt = getSystemPromptForProfile(profile, roleContext = \"tutor\")\n                    val systemPrompt = \"\"\"",
        "val basePrompt = getSystemPromptForProfile(profile, roleContext = \"tutor\")\n                    val systemPrompt = \"\"\"\n                        $personalizationProfile"
      )

      // Record real learning outcomes so future sessions can adapt to observed performance.
      text = text.replace(
        "_isAnswerCorrect.value = isCorrect",
        "_isAnswerCorrect.value = isCorrect\n        com.example.learning.LearnerPersonalizationEngine.recordAnswer(getApplication(), _currentProfile.value.id, _selectedSubject.value.name, isCorrect, question.questionText.take(100))"
      )
      viewModel.writeText(text)
    }

    val location = file("src/main/java/com/example/util/LocationComplianceHelper.kt")
    if (location.exists()) {
      var text = location.readText()
      val replacements = linkedMapOf(
        "val isGoogleMapsVerified: Boolean = true" to "val isGoogleMapsVerified: Boolean = false",
        "val resolutionSource: String = \"Google Maps Location Service\"" to "val resolutionSource: String = \"Android Geocoder / postal resolver\"",
        "isGoogleMapsVerified = true" to "isGoogleMapsVerified = false",
        "complianceMessage = \"🗺️ Resolved via Google Maps Geocoding ($clean): Aligned to" to "complianceMessage = \"🗺️ Resolved via Android Geocoder / postal resolver ($clean): Aligned to",
        "resolutionSource = \"Google Maps Geocoding (ZIP/Postal Fallback)\"" to "resolutionSource = \"Android Geocoder / ZIP-postal fallback\""
      )
      replacements.forEach { (old, new) -> text = text.replace(old, new) }
      location.writeText(text)
    }
  }
}

tasks.configureEach {
  if (name.contains("Kotlin", ignoreCase = true) && name.contains("Compile", ignoreCase = true)) {
    dependsOn(normalizeLegacyIntegrations)
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
