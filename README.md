# NeuroPath 🧠✨
### Adaptive, Sensory-Friendly Educational Platform for Neurodiverse and Curious Minds

Created by **FourgeAI LABS** ([https://github.com/fourgeailabs](https://github.com/fourgeailabs))

[![Build Status](https://github.com/fourgeailabs/neuropath/actions/workflows/build.yml/badge.svg)](.github/workflows/build.yml)
[![Version](https://img.shields.io/badge/version-1.06.00-blue.svg)](app/build.gradle.kts)
[![Android](https://img.shields.io/badge/Platform-Android_14_%2B-green.svg)](app/build.gradle.kts)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-purple.svg)](app/build.gradle.kts)
[![License](https://img.shields.io/badge/License-Apache_2.0-orange.svg)](LICENSE)

---

## 🌟 Overview

**NeuroPath** is a comprehensive, sensory-conscious mobile learning application built with Kotlin and Jetpack Compose by **FourgeAI LABS**. Tailored specifically for children with ADHD, Autism, Dyslexia, Dyscalculia, and all curious learners, NeuroPath blends granular local school district educational requirements with personalized interest worlds, regulation tools, global multi-language compatibility, pre-installed K-12 OER Commons curriculum materials, and speech-to-text voice assist.

---

## 🚀 Key Features in Recent Updates

### 📚 1. Pre-Installed OER Commons K-12 Curriculum Collection (v1.06.00)
* **Pre-Installed & Offline-First**: Complete curated K-12 curriculum catalog from [OER Commons](https://oercommons.org/curated-collections) is pre-installed directly into the app's local Room database upon startup, guaranteeing full offline access to curriculum benchmarks.
* **Curriculum-Aware AI Tutor Retrieval**: The AI Learning Buddy (Gemini AI & Live Voice Assist) dynamically retrieves relevant OER curriculum context for the child's exact grade level, subject, and school district to ground explanations, examples, and practice questions.
* **Live Sync Service**: Background HTTP service to fetch and parse updated OER Commons collections with an automated fallback to the pre-installed catalog.
* **Multi-Language Support**: OER curriculum materials and AI tutor retrieval work seamlessly across all 21 supported app languages.

### 🌐 2. Full 21-Language Global Localization (v1.05.00)
* **App-Wide Reactive Propagation**: Selecting or switching a language in Setup, Parent Settings, or Profile Selection immediately translates every UI component, screen, dialogue, sensory suite tool, and lesson instruction.
* **Full 21-Language Matrix**: Complete translations across:
  * 🇺🇸 English (American) & 🇬🇧 English (British)
  * 🇪🇸 Spanish (`Español`)
  * 🇫🇷 French (`Français`)
  * 🇩🇪 German (`Deutsch`)
  * 🇨🇳 Mandarin Chinese (`中文 (普通话)`)
  * 🇯🇵 Japanese (`日本語`)
  * 🇰🇷 Korean (`한국어`)
  * 🇧🇷 Portuguese (`Português`)
  * 🇮🇹 Italian (`Italiano`)
  * 🇳🇱 Dutch (`Nederlands`)
  * 🇸🇪 Swedish (`Svenska`)
  * 🇷🇺 Russian (`Русский`)
  * 🇹🇷 Turkish (`Türkçe`)
  * 🇵🇱 Polish (`Polski`)
  * 🇬🇷 Greek (`Ελληνικά`)
  * 🇻🇳 Vietnamese (`Tiếng Việt`)
  * 🇹🇭 Thai (`ไทย`)
  * 🇮🇩 Indonesian (`Bahasa Indonesia`)
  * 🇮🇳 Hindi (`हिंदी`)
  * 🇸🇦 Arabic (`العربية`)

---

## 🏛️ About FourgeAI LABS

NeuroPath is created and maintained by **FourgeAI LABS**.
* Creator GitHub: [https://github.com/fourgeailabs](https://github.com/fourgeailabs)
* App Repository: [https://github.com/fourgeailabs/neuropath](https://github.com/fourgeailabs/neuropath)
* App Identifier: `com.fourgeailabs.neuropath`

---

## 📝 Changelog & Release History

### **v1.06.00** (Current Version)
* 📚 **Pre-Installed OER Commons Service**: Built an offline-first service that parses and seeds the curated OER Commons collection (`https://oercommons.org/curated-collections`) covering all K-12 grades (Kindergarten to Grade 12) across Mathematics, English Language Arts, Sciences, Social Studies & Civics.
* 🧠 **AI Tutor Curriculum Retrieval**: Integrated `retrieveOerTutorContext` directly into the Gemini Socratic chat and Live Voice conversation flows to supply rich grade-level benchmarks.
* 🌐 **Full 21-Language OER Support**: OER retrieval and AI tutor synthesis operate seamlessly across all 21 languages in the application.

### **v1.05.00**
* 🌐 **21-Language Global Localization**: Full multi-language dictionary across English (US/UK), Spanish, French, German, Mandarin, Japanese, Korean, Portuguese, Italian, Dutch, Swedish, Russian, Turkish, Polish, Greek, Vietnamese, Thai, Indonesian, Hindi, and Arabic.
* ⚡ **Complete Modal & Dialog Localization**: Fully translated "What's New", "Check for Updates", Parent Dashboard Tabs, Setup Onboarding, and Sensory Suite controls.
* 🔄 **App Auto-Update Feature**: Real-time update checks against GitHub Releases with options to update, remind later, or skip version.

### **v1.04.00**
* 🌐 **Global Language Switching**: Complete reactive app-wide localization covering all menus, setup steps, parent settings, sensory bar controls, and dashboard views.
* 🧠 **Socratic AI Research Assistant**: Deep offline and online curriculum retrieval engine grounded in official localized standards and downloaded offline materials.
* 📚 **OER Commons K-12 Integration**: Comprehensive curriculum integration across elementary, middle, and high school grades.

### **v1.03.00**
* 🌐 **OER Commons Database Integration**: Live pull and alignment with OER Commons Curated Collections (`https://oercommons.org/curated-collections`) for all K-12 grades in the US.
* 🎓 **High School (Grades 9-12) Curriculum Spectrum**: Comprehensive lesson plans and benchmarks for Algebra I & II, Geometry, Quadratic Functions, Rhetoric, Cellular Biology, Physics, Civics, Economics, and Personal Finance.
* 🔬 **AI Curriculum Research Assistant**: Context-aware assistant providing instantaneous access to state and district standards with interactive prompt chips.

### **v1.02.00**
* 🤖 **Universal Gemini Free Tier AI Core**: App automatically utilizes the built-in free tier Gemini AI secret across all speech-to-text dictation, personalized tutoring, adaptive hints, and text reviews.
* 📜 **Scroll-Mandated Terms & Conditions**: Dedicated NeuroPath Terms & Conditions with scroll-to-bottom reading requirement and COPPA privacy acceptance before app activation.
* ⚠️ **AI Accuracy & Safety Disclaimer**: Prominent disclaimer in settings clarifying that AI models can make mistakes, with parental guidance encouraged.
* 🔐 **Parental Passcode Security**: 4-digit PIN setup with zero default passcodes protecting parent controls and analytics.

### **v1.01.00**
* 🎵 **Google Lyria AI Music Generation**: Real-time procedural ambient soundscapes tailored to student sensory needs.
* 🧘 **4-7-8 Breathing Guide**: Pacing visual rings and haptic pop-it fidget feedback.

### **v1.00.00**
* 🧠 **Initial Release**: Core Jetpack Compose UI, hyper-interest thematic lessons, Room database, and COPPA privacy compliance.
