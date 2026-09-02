# NeuroPath 🧠✨
### Adaptive, Sensory-Friendly Educational Platform for Neurodiverse and Curious Minds

Created by **FourgeAI LABS** ([https://github.com/fourgeailabs](https://github.com/fourgeailabs))

[![Build Status](https://github.com/fourgeailabs/neuropath/actions/workflows/build.yml/badge.svg)](.github/workflows/build.yml)
[![Version](https://img.shields.io/badge/version-1.03.00-blue.svg)](app/build.gradle.kts)
[![Android](https://img.shields.io/badge/Platform-Android_14_%2B-green.svg)](app/build.gradle.kts)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-purple.svg)](app/build.gradle.kts)
[![License](https://img.shields.io/badge/License-Apache_2.0-orange.svg)](LICENSE)

---

## 🌟 Overview

**NeuroPath** is a comprehensive, sensory-conscious mobile learning application built with Kotlin and Jetpack Compose by **FourgeAI LABS**. Tailored specifically for children with ADHD, Autism, Dyslexia, Dyscalculia, and all curious learners, NeuroPath blends granular local school district educational requirements with personalized interest worlds, regulation tools, global multi-language compatibility, and speech-to-text voice assist.

By pairing core educational milestones with topics kids love (Dinosaurs, Outer Space, Deep Ocean, Superheroes, Mythical Creatures, and Robotics), NeuroPath transforms learning into an engaging, low-anxiety quest.

---

## 🚀 Key Features in Recent Updates

### 🌐 1. OER Commons Curated Collections Integration (K-12 & High School)
* **Live Online Database Synchronization**: Synchronizes standards directly from **[OER Commons Curated Collections](https://oercommons.org/curated-collections)** for all US grades K-12.
* **Full High School Spectrum (Grades 9-12)**: Covers Algebra I & II, Geometry, Quadratic Functions, Rhetorical Analysis, World Literature, Cellular Biology, Newtonian Physics, US Civics, Constitutional Law, Macroeconomics, and Personal Finance.
* **Secondary Tier Lesson Mapping**: Dynamic curriculum catalog with level-appropriate modules for Elementary, Middle School, and High School.

### 🔬 2. AI Curriculum Research Assistant
* **Instant Curriculum Retrieval**: Ask the AI Research Assistant to search and retrieve standards, benchmarks, and learning goals for any subject or grade.
* **Flexible Learning Modalities**: Provides direct answers, full solutions, or step-by-step guidance on demand.
* **Mathematical Accuracy & Verification**: Exact mathematical parsing and multi-step solver with strict validation for correct and incorrect answers.

### 🎙️ 3. Free Gemini & Native Speech-to-Text (STT) Integration
* **Natural Voice Dictation**: Speak directly into the microphone in any chat or learning screen using free Gemini API audio processing or built-in voice recognition.
* **Dual Voice Assist (TTS + STT)**: Children can both listen to lessons read aloud with karaoke word highlighting and dictate their responses effortlessly.

### 🏛️ 4. Granular Educational Requirements
* **4-Level Location Granularity**: Select **Country**, **State / Province**, **City**, and **School District / Local Education Authority**.
* **District-Level Curriculum Alignment**: Pulls specific curriculum benchmarks (e.g., LAUSD CA-CCSS, Toronto District School Board Ontario Curriculum, London LEA National Curriculum, Tokyo Metropolitan Board of Education).
* **Global Presets**: Built-in quick select presets across North America, Europe, Asia, Australia, and South America.

### 🌐 5. Baked-In Global Language Selector
* **10 Native App Languages**: Full compatibility across English, Spanish, French, German, Mandarin, Japanese, Portuguese, Hindi, Arabic, and Italian.
* **Global Accessibility**: Language selection dynamically adjusts UI labels, TTS voice synthesis, and Gemini AI tutor prompting.

---

## 🎨 Visual Identity & Sensory Tools

* **Neurodiversity Accommodations**: OpenDyslexic typography support, high-contrast and low-stimulation color modes, TTS speech rate/pitch sliders.
* **Sensory Tools**: Interactive 16-bubble haptic Pop-It fidget board and animated 4-7-8 breathing circle guide.
* **Interest Worlds**: Dinosaur, Space, Ocean, Mythical Creatures, Superhero, and Robot themes.
* **NeuroBuddy AI Tutor**: Gemini-powered empathetic AI companion providing Socratic hints and growth-mindset coaching.

---

## 🔒 Parent & Educator Dashboard

* **PIN Protected Gate**: Secure access control for parents and teachers.
* **Granular Location Settings**: Configure Country, State, City, and District curricula.
* **Language & Neurodiversity Switches**: Toggle language presets, dyslexia fonts, TTS speeds, and ADHD/Autism accommodation tags.
* **Learning Analytics**: Track completed lessons, accuracy, sensory breaks, and subject mastery.

---

## 🏛️ About FourgeAI LABS

NeuroPath is created and maintained by **FourgeAI LABS**.
* GitHub Repository & Releases: [https://github.com/fourgeailabs](https://github.com/fourgeailabs)
* App Identifier: `com.fourgeailabs.neuropath`

---

## 📝 Changelog & Release History

### **v1.03.00** (Current Version)
* 🌐 **OER Commons Database Integration**: Live pull and alignment with OER Commons Curated Collections (`https://oercommons.org/curated-collections`) for all K-12 grades in the US.
* 🎓 **High School (Grades 9-12) Curriculum Spectrum**: Comprehensive lesson plans and benchmarks for Algebra I & II, Geometry, Quadratic Functions, Rhetoric, Cellular Biology, Physics, Civics, Economics, and Personal Finance.
* 🔬 **AI Curriculum Research Assistant**: Context-aware assistant providing instantaneous access to state and district standards with interactive prompt chips.
* ⚡ **Flexible Problem Solving & Verification**: Removed forced walkthrough restrictions; added direct math calculations, step-by-step guidance, and rigorous answer verification.

### **v1.02.00**
* 🤖 **Universal Gemini Free Tier AI Core**: App now automatically utilizes the built-in free tier Gemini AI secret across all speech-to-text dictation, personalized tutoring, adaptive hints, and text reviews.
* 📜 **Scroll-Mandated Terms & Conditions**: Dedicated NeuroPath Terms & Conditions with scroll-to-bottom reading requirement and COPPA privacy acceptance before app activation.
* ⚠️ **AI Accuracy & Safety Disclaimer**: Prominent disclaimer in settings clarifying that AI models can make mistakes, with parental guidance encouraged.
* 🛠️ **BYO AI Provider Settings**: Optional parent settings panel to supply a custom third-party API key (Google Gemini, OpenAI, Claude, Custom REST) if preferred over the included free tier.
* 💡 **Lightbulb App Icon**: Custom visual lightbulb icon with yellow wave interior and dual cyan/gold sparkles on setup screen.
* 🔐 **Parental Passcode Security**: 4-digit PIN setup with zero default passcodes protecting parent controls and analytics.
* 🎙️ **Speech-to-Text (STT)**: Voice dictation using Gemini API speech processing online or Android SpeechRecognizer.
* 🏛️ **Granular Educational Standards**: Country, State/Province, City, and School District level curriculum alignment.
* 🌐 **Baked-In Global Language Selector**: 20 native languages with instant UI translation.

### **v1.01.00**
* ⚡ **Self-Healing Gradle Decoder**: Automatic base64 debug keystore decoding for Android CI/CD pipelines.
* 🧘 **4-7-8 Breathing Guide**: Pacing visual rings and haptic pop-it fidget feedback.

### **v1.00.00**
* 🧠 **Initial Release**: Core Jetpack Compose UI, hyper-interest thematic lessons, Room database, and COPPA privacy compliance.
