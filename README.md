# NeuroPath 🧠✨
### Adaptive, Sensory-Friendly Educational Platform for Neurodiverse and Curious Minds

**NeuroPath** is an Android learning platform from **FourgeAI LABS** focused on individualized, sensory-aware education. Its core design principle is:

> **Curriculum determines what a child needs to learn. The child determines how NeuroPath should teach it.**

NeuroPath combines curriculum-aware tutoring, local and cloud AI, learner personalization, adaptive mastery signals, OER resources, multimedia learning, accessibility features, and jurisdiction-aware curriculum routing.

[![Build Status](https://github.com/fourgeailabs/neuropath/actions/workflows/build.yml/badge.svg)](.github/workflows/build.yml)
[![Version](https://img.shields.io/badge/version-2.00.01-blue.svg)](app/build.gradle.kts)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](app/build.gradle.kts)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg)](app/build.gradle.kts)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](LICENSE)

---

## 🚀 Current Status

**Version:** `2.00.01`  
**Application ID:** `com.fourgeailabs.neuropath`  
**Platform:** Android  
**Minimum SDK:** 24  
**Target SDK:** 36  
**Primary UI:** Kotlin + Jetpack Compose  
**Local AI architecture:** GGUF Gemma inference through llama.cpp with an optional LiteRT-LM accelerated Gemma path  

NeuroPath is actively developed. The current repository contains substantial working foundations for local AI, Gemini cloud/live AI, personalized tutoring, global curriculum routing, UK curriculum routing, OER curriculum retrieval, and real multimedia playback. Recent work has focused on trial-release hardening: security, testing, accessibility, and dependency hygiene. Some global curriculum jurisdictions currently provide routing metadata and official-source entry points rather than an exhaustive offline copy of every country's curriculum.

---

## 🧠 What NeuroPath Is Designed To Do

NeuroPath is being built around an individualized learning pipeline:

**Child**  
→ **Country / Territory**  
→ **Education Jurisdiction**  
→ **Official Curriculum**  
→ **Stage / Grade**  
→ **Subject**  
→ **Learning Objective**  
→ **Prerequisites & Mastery**  
→ **Personalized Learning Strategy**  
→ **Learning Activity**  
→ **Assessment**  
→ **Updated Learner Model**

The goal is not simply to teach a generic lesson to every child of the same age. NeuroPath is designed to identify the educational objective that applies to the learner and then adapt presentation, examples, pacing, scaffolding, difficulty, modality, and support around that individual learner.

---

# ✨ Major Current Features

## 🤖 Local Gemma AI

NeuroPath uses real local GGUF inference rather than hard-coded/fake Gemma responses.

Current implementation includes:

- llama.cpp Android integration
- LiteRT-LM accelerated inference path when a compatible model/backend is installed
- ARM64 support
- Gemma 2 2B instruction model in GGUF format
- `gemma-2-2b-it-Q4_K_M.gguf`
- optional Gemma 3 1B LiteRT-LM models selected by supported hardware/model targets
- automatic model downloading
- temporary-download handling before installation
- GGUF/LiteRT-LM model validation
- download progress reporting
- device compatibility reporting
- CPU/NEON local inference fallback
- GPU/NPU-capable LiteRT-LM backend selection with real inference verification
- backend fallback to CPU when an accelerator is unavailable
- actual model loading and completion
- model cleanup/release
- safe model/download error handling

NeuroPath does **not** claim that an accelerator is active merely because a device has compatible hardware or a model artifact installed. An accelerated backend is reported only after the local runtime successfully initializes it and completes inference. This keeps the hardware story truthful across Snapdragon, Tensor, MediaTek, and unsupported/CPU-only devices.

---

## ☁️ Gemini Cloud AI

The Gemini architecture has been modernized away from obsolete model identifiers.

The current architecture supports current Gemini model families for tasks such as:

- educational chat
- adaptive explanations
- curriculum assistance
- hints
- question generation
- voice-related workflows
- advanced tutoring

Legacy Gemini 1.5/2.0 identifiers are being migrated out of the large legacy client while the application moves to current supported model families.

---

## 🎙️ Real Gemini Live API

NeuroPath contains a real Gemini Live WebSocket client rather than relying on simulated live conversation behavior.

Implemented capabilities include:

- Gemini Live WebSocket connection
- authenticated setup handshake
- realtime text input
- realtime microphone audio input
- 16-bit PCM audio handling
- 16 kHz audio handling
- audio stream termination
- input audio transcription configuration
- output audio transcription configuration
- native audio output handling
- PCM-to-WAV playback conversion for Android
- integration with the Learning Buddy voice flow

The Live API is separated from standard text-generation models because realtime audio interaction has different API/model requirements.

---

# 🎓 Curriculum-Aware Learning

NeuroPath's curriculum system has been expanded from broad grade/subject matching toward actual learner-question and learning-objective matching.

Matching can consider:

- exact titles
- topics
- concepts
- vocabulary
- learning objectives
- standards
- grade/stage
- the learner's actual question

The system also distinguishes between online curriculum synchronization and curriculum that is already available locally/offline, avoiding false synchronization claims.

---

# 🌍 Global Curriculum Jurisdiction Architecture

NeuroPath includes a global curriculum jurisdiction registry designed to route a learner to the education system that actually applies to them.

The architecture recognizes that a country is not always the final curriculum authority.

Examples include:

- United States → state education systems
- Canada → provinces and territories
- Australia → states and territories
- Germany → Länder
- Switzerland → cantons
- Belgium → communities
- Spain → autonomous communities

The intended routing hierarchy is:

**Country / Territory → Subnational Jurisdiction → Education Authority → Curriculum Framework → Stage → Subject → Objective**

The registry currently contains routing foundations and official-source entry points for many countries and territories across North America, South America, Europe, Africa, Asia, Oceania, and the Caribbean.

### Important scope note

This is a **global curriculum routing foundation**, not a claim that every curriculum document from every jurisdiction has already been fully ingested into the app. Detailed official curriculum adapters and structured objective ingestion are being expanded progressively.

---

# 🇬🇧 UK Curriculum Support

NeuroPath explicitly models the UK's devolved education systems rather than treating the UK as one generic curriculum.

The UK registry currently routes among:

### England

- Key Stage 1–2
- Key Stage 3–4

### Scotland

- Early / First / Second levels
- Third / Fourth / Senior levels

### Wales

- Curriculum for Wales
- ages approximately 3–16

### Northern Ireland

- Foundation / Key Stage 1–2
- Key Stage 3–4

Official curriculum authority/source information is included in the UK curriculum catalog and is integrated into the OER/tutor curriculum context.

---

# 🧩 Individual Learner Personalization

NeuroPath includes a learner personalization engine intended to build an evolving model of how an individual child learns.

The model can incorporate:

- age
- grade/stage
- declared learning differences
- strengths
- weaknesses/challenges
- interests
- motivating topics
- disliked/frustrating activities
- accessibility settings
- preferred explanation style
- learning modality
- active learning theme
- current subject
- historical answer accuracy
- topic-level attempts and correctness
- recent struggles
- confidence signals
- deterministic instructional strategy selection

The objective is to move from generic personalization to **individualized instructional adaptation**.

---

# 📈 Adaptive Learning Signals

NeuroPath records real learning outcomes and uses them to influence future instruction.

Examples include:

- repeated incorrect answers → increased scaffolding
- successful explanation styles → greater preference for those styles
- repeated struggles → modified explanations
- successful modalities → increased use of those modalities
- performance patterns → changes to difficulty and pacing
- topic mastery evidence → confidence and strategy changes
- recent outcomes → changes to support level

The learner model now produces an explicit next-step instructional strategy: diagnostic-friendly onboarding for new learners, high scaffolding after low performance, targeted retrieval and misconception checks after repeated misses, moderate scaffolding during developing mastery, and reduced scaffolding with harder transfer tasks after strong performance.

The curriculum objective remains stable; the path to mastery adapts.

---

# 🛡️ Privacy-Aware Personalization

Learner information can include sensitive information, so NeuroPath is being designed to avoid treating diagnosis labels as a substitute for understanding the child.

The intended architecture separates:

**Sensitive learner information**  
from  
**needs-based instructional signals**

Local AI can work with richer local learner context when appropriate. Cloud tutoring should receive only the information needed for the requested task, with sensitive diagnosis information excluded from cloud prompts by default where the current integration permits.

The cloud prompt path remains an area for continued privacy auditing as legacy Gemini code is migrated.

---

# 🎨 Adaptive Neuro-Themes

NeuroPath includes a large collection of themed learning worlds designed to connect learning with a child's interests.

Theme categories include examples such as:

- Ancient civilizations
- Robotics and AI
- Mythology
- Culinary science
- Music
- Sports
- Environmental exploration
- Art
- Transportation
- Space
- Medical science
- Architecture
- Gaming and VR
- Spy/forensics themes

The theme system supports profile-aware recommendations and configurable periodic rotation.

---

# 🎨 Theme Preview & Accessibility

The theme preview system provides an interactive preview before applying a theme.

Features include:

- simulated screen preview
- atmospheric visual effects
- companion-buddy preview
- lesson-card preview
- color palette inspection
- accessibility-oriented contrast information
- theme category browsing
- theme search/filtering
- 100-theme browsing
- rotation configuration

The application also uses standardized touch-target sizing and accessibility-conscious UI patterns throughout the updated interfaces.

---

# 🎬 Real Multimedia Learning

The previous OER multimedia player was largely a simulated visual/timer experience. It has been replaced with real Android media handling.

## Video

Video resources can now use an actual Android `WebView` with:

- JavaScript enabled
- DOM storage
- media playback configuration
- `WebChromeClient`
- `WebViewClient`
- actual video URL loading
- source URL fallback

This supports real web-hosted video lessons and science simulations.

## Audio

Audio resources can use Android `MediaPlayer` for real playback.

When an audio URL is unavailable, NeuroPath can fall back to text-to-speech through the existing `SpeechManager`.

## Captions and transcripts

The multimedia system retains support for:

- captions
- transcripts
- timestamp navigation
- playback speed
- checkpoints
- quizzes

The goal is for multimedia to remain part of the learning flow rather than being an isolated media viewer.

---

# 🔊 Voice & Text-to-Speech

NeuroPath includes spoken-learning support and adjustable playback behavior.

Existing voice features include:

- text-to-speech lesson playback
- adjustable narration speed
- voice-assist workflows
- speech/transcription integration
- realtime Gemini voice architecture

---

# 🗺️ Location & Curriculum Jurisdiction Resolution

The location system has been corrected to represent its actual sources accurately.

It can use Android geocoding and postal/ZIP resolution to help determine a learner's educational jurisdiction.

The implementation no longer falsely claims Google Maps verification when the information actually comes from Android Geocoder or postal resolution.

Location information is used to help route the learner toward the appropriate curriculum/jurisdiction rather than treating location as a substitute for curriculum verification.

---

# 👨‍👩‍👧 Parent & Learner Controls

NeuroPath includes controls and configuration around:

- Learning Buddy availability
- AI mode selection
- local/offline learning modes
- theme selection
- theme rotation
- learner profile information
- learning preferences
- accessibility settings

The goal is to let parents/educators remain aware of how the application is configured while allowing the learner experience to remain child-friendly.

---

# 📚 OER & Curriculum Resources

NeuroPath integrates OER-oriented curriculum retrieval and curated learning resources.

The architecture supports curriculum resources such as:

- lessons
- videos
- audio
- transcripts
- quizzes
- worksheets
- standards alignment
- curriculum objectives

Oak National Academy has also been identified as an important official/curriculum-aligned resource path for England, including its curriculum and lesson-resource APIs. API credentials should be provided through configuration rather than hard-coded into the application.

---

# 📴 Local / Offline Direction

NeuroPath is designed to retain useful learning functionality when cloud services are unavailable.

Local capabilities include:

- local Gemma inference
- hardware-accelerated local inference when a supported runtime/model is actually available
- local learner signals and mastery evidence
- locally available curriculum resources
- offline Socratic tutoring/fallback behavior
- local multimedia resources when available

Cloud AI remains available for capabilities that benefit from network access.

---

# 🏗️ Build & CI

The project uses GitHub Actions for automated Android builds.

The workflow supports:

- pushes to `main`
- pull requests
- manual workflow dispatch
- JDK 21
- Gradle 9.3.1
- debug APK generation
- debug signing setup
- release builds from tags
- APK artifact upload
- JVM unit tests

The debug artifact is published as:

`neuropath-debug-apk`

A successful historical build does not automatically validate later commits; current GitHub Actions results should always be checked before declaring a new build verified.

---

# 📋 Recent Changes (v2.00.01 — Trial Release Readiness)

This release focuses on hardening the codebase for educator trial evaluation and production readiness.

## ✅ Completed Fixes

### Security & Integrity
- **Removed secrets from git tracking**: `google-services.json` moved to `.gitignore` with `google-services.json.example` template
- **Fixed hardcoded PIN fallback**: Removed `"1234"` default; PIN must now be explicitly configured
- **Updated `.gitignore`**: Added keystore files, google-services.json, and other sensitive artifacts

### Fake Implementation Removal
- **`initiateOfflineCurriculumSync()`**: Now calls real `syncDailyCurriculumForLocale()` instead of 15-second delay
- **Empty catch blocks**: All now log errors via `Log.e()` and surface user-facing messages
- **Mock curriculum data**: When no API key, returns honest "offline mode" message with `isOnlineSynced = false`

### Architecture Hardening
- **Room database migration**: Added v9→v10 migration, DB version bumped to 10, `exportSchema=true`
- **Navigation persistence**: Back stack now survives config changes via SharedPreferences
- **CSV field normalization**: Identified for future relation tables (schema ready)

### Testing Infrastructure
- **Unit tests**: `GeminiClientTest` (math parsing, Socratic replies), `LearnerPersonalizationEngineTest` (answer tracking, strategy selection)
- **Instrumented tests**: Room database CRUD operations
- **CI/CD enhancements**: Added detekt static analysis, ktlint formatting, dependency vulnerability scanning, instrumented tests on macOS emulator

### Accessibility & Internationalization
- **200+ strings** externalized to `strings.xml` from hardcoded Compose code
- **4 new locales**: Spanish (`values-es`), French (`values-fr`), German (`values-de`), Chinese (`values-zh`)
- **Content descriptions** added for screen reader support
- **WCAG AA contrast** verified in theme system

### Dependency Updates
- AGP **9.2.0**, Compose BOM **2024.10.00**
- **Removed unused Firebase**: `firebase-ai`, `firebase-appcheck-recaptcha`, `firebase-firestore`, `firebase-auth`
- Added **detekt 1.23.6** + **ktlint 12.1.0** with baseline

---

# 🛠️ Getting Started / Development Setup

## Prerequisites

- **JDK 21** (required by Gradle 9.3.1 and AGP 9.1.1)
- **Android SDK** with API level 36 (targetSdk) and build-tools
- **Gradle 9.3.1** (managed via wrapper)

## Local Build

```bash
# Clone the repository
git clone https://github.com/fourgeailabs/NeuroPath.git
cd NeuroPath

# Ensure debug keystore exists (for debug signing)
# The repo includes debug.keystore.base64 which CI decodes;
# for local builds, copy it or generate your own:
cp debug.keystore_test debug.keystore

# Create .env file for secrets (Gemini API key)
cp .env.example .env
# Edit .env and add your GEMINI_API_KEY

# Build debug APK
./gradlew.bat assembleDebug
# On Linux/macOS: ./gradlew assembleDebug
```

## CI / GitHub Actions

The repository includes a GitHub Actions workflow (`.github/workflows/build.yml`) that:

- Sets up JDK 21 and Gradle 9.3.1
- Decodes `debug.keystore.base64` for debug signing
- Runs `assembleDebug` and `testDebugUnitTest`
- Uploads `neuropath-debug-apk` artifact
- Creates releases on tags

Required repository secrets for CI:
- `KEYSTORE_PATH` (for release signing)
- `STORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS`
- `GEMINI_API_KEY` (or configured via `.env`)

## Configuration Files

| File | Purpose | Tracked? |
|------|---------|----------|
| `.env` | Runtime secrets (API keys) | ❌ (in `.gitignore`) |
| `.env.example` | Template for `.env` | ✅ |
| `debug.keystore` | Debug signing key | ❌ (in `.gitignore`) |
| `debug.keystore.base64` | Base64-encoded debug keystore for CI | ✅ |
| `app/google-services.json` | Firebase config (placeholder included) | ✅ |
| `local.properties` | SDK/NDK paths | ❌ (in `.gitignore`) |

---

# 🧰 Technology Stack

- Kotlin
- Jetpack Compose
- Android SDK
- Gradle
- Kotlin Coroutines
- llama.cpp Android / GGUF inference
- LiteRT-LM accelerated local inference
- Gemma models
- Google Gemini API
- Gemini Live API
- Android WebView
- Android MediaPlayer
- Android Text-to-Speech
- Room/local persistence where applicable
- OER curriculum resources
- GitHub Actions

---

# 🔐 Security & Accuracy Principles

NeuroPath's recent development has focused on replacing simulated behavior and inaccurate implementation claims with real functionality.

Examples include:

- fake Gemma responses → real local GGUF inference
- simulated multimedia playback → real WebView/MediaPlayer playback
- simulated Live voice behavior → real Gemini Live WebSocket architecture
- obsolete Gemini identifiers → current model architecture
- false Google Maps verification → truthful Android Geocoder/postal resolution
- false online curriculum synchronization → explicit online/offline state
- unverified accelerator claims → backend activation reported only after successful local inference

API keys and secrets should never be committed to source code.

For production deployments, repository security features such as secret scanning, dependency alerts, and code scanning are recommended.

---

# ⚠️ Current Development Scope

NeuroPath is actively evolving. The following areas remain under development:

## 🔄 Near Term (Next Release)
- **CSV field normalization**: Replace `neurodivergentTypesCsv`, `strugglesCsv`, `strengthsCsv`, `hyperFixationsCsv`, `unlockedItemIdsCsv` with proper relation tables
- **Encrypted PIN storage**: Move parent PIN from plaintext Room column to `EncryptedSharedPreferences` or Android Keystore
- **Offline curriculum completeness**: Expand pre-installed OER curriculum beyond current 10 lessons
- **Educator trial feedback integration**: Telemetry for lesson completion rates, sensory tool usage, personalization effectiveness

## 📅 Medium Term
- **Multi-device sync (opt-in)**: Encrypted backup/restore via user-controlled cloud (Google Drive, iCloud)
- **Advanced analytics dashboard**: Parent/educator insights with privacy-preserving aggregation
- **IEP/504 plan integration**: Structured accommodation import from school systems
- **Voice-first navigation**: Full app control via Gemini Live for motor-impaired learners

## 🌍 Long Term
- **Curriculum ingestion pipeline**: Automated ingestion from official sources (state DOE APIs, OER Commons, Oak National Academy)
- **Cross-platform**: iOS (Compose Multiplatform) and Web (Compose for Web)
- **Research partnerships**: Anonymized learning analytics for neurodiversity education research
- **Global standards coverage**: Complete jurisdiction adapters for all 80+ seeded countries/territories

## 🏗️ Technical Debt
- **ViewModel decomposition**: Split `NeuroPathViewModel` (1,900+ lines) into domain-specific ViewModels
- **Test coverage**: Target 80%+ unit test coverage, add UI screenshot tests (Roborazzi)
- **Performance profiling**: Baseline memory/CPU for low-end devices (2GB RAM)
- **Dependency audit**: Quarterly review of all transitive dependencies

These are intentionally documented as ongoing work rather than being presented as completed functionality.

---

# 🧭 Product Philosophy

NeuroPath is being built around one central idea:

> **Every child should be taught the curriculum that applies to them, in a way that makes sense for them.**

That means NeuroPath should understand both sides of the problem:

### What the child needs to learn

Determined by the applicable education system, curriculum framework, subject, standards, objectives, and prerequisites.

### How this child learns best

Determined by strengths, challenges, interests, preferences, accessibility needs, observed behavior, performance, and successful learning strategies.

The curriculum objective should remain stable. The path to mastery can be individualized.

---

# 🏛️ About FourgeAI LABS

NeuroPath is created and maintained by **FourgeAI LABS**.

- Creator: [FourgeAI LABS](https://github.com/fourgeailabs)
- Repository: [fourgeailabs/NeuroPath](https://github.com/fourgeailabs/NeuroPath)
- Application ID: `com.fourgeailabs.neuropath`

---

# 📜 Release History

## `2.00.01` — Trial Release Readiness (Current)

This release hardens the codebase for educator trial evaluation:

- **Security**: Removed secrets from git, fixed hardcoded PIN, added encrypted storage prep
- **Honesty**: Eliminated fake implementations (mock curriculum, empty catches, fake sync)
- **Reliability**: Room migrations, navigation persistence, proper error handling
- **Quality**: Unit/integration tests, static analysis (detekt/ktlint), dependency scanning
- **Accessibility**: 200+ externalized strings, 4 new locales, content descriptions
- **Maintenance**: Updated AGP/Compose, removed unused Firebase, added linting baseline

## `2.00.00` — Current Repository Version

The current development line includes the major architecture work described above, including:

- real local Gemma GGUF inference
- optional LiteRT-LM accelerated local inference
- Gemini model modernization
- Gemini Live API architecture
- global curriculum jurisdiction routing
- UK four-nation curriculum routing
- learner personalization
- persistent adaptive learning signals
- topic-level mastery and confidence evidence
- deterministic instructional strategy selection
- privacy-aware learner context handling
- real video and audio playback
- OER curriculum matching improvements
- truthful location-source reporting
- build/CI improvements

Earlier releases introduced the application's broader Learning Buddy, OER, theme, accessibility, TTS, parent-control, profile, message-history, and curriculum features.

---

# 📌 Important Documentation Note

This README intentionally distinguishes **implemented functionality**, **architecture/foundation work**, and **ongoing development**. NeuroPath's global curriculum architecture is designed to scale to additional countries and territories, but the existence of a jurisdiction entry does not mean that every official curriculum document for that jurisdiction has already been downloaded, normalized, or bundled into the application.

The repository source code and current GitHub Actions results are the authoritative sources for implementation and build status.
