# NeuroPath 🧠✨
### Adaptive, Sensory-Friendly Educational Platform for Neurodiverse and Curious Minds

Created by **FourgeAI LABS** ([https://github.com/fourgeailabs](https://github.com/fourgeailabs))

[![Build Status](https://github.com/fourgeailabs/neuropath/actions/workflows/build.yml/badge.svg)](.github/workflows/build.yml)
[![Version](https://img.shields.io/badge/version-1.14.00-blue.svg)](app/build.gradle.kts)
[![Android](https://img.shields.io/badge/Platform-Android_14_%2B-green.svg)](app/build.gradle.kts)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-purple.svg)](app/build.gradle.kts)
[![License](https://img.shields.io/badge/License-Apache_2.0-orange.svg)](LICENSE)

---

## 🌟 Overview

**NeuroPath** is a comprehensive, sensory-conscious mobile learning application built with Kotlin and Jetpack Compose by **FourgeAI LABS**. Tailored specifically for children with ADHD, Autism, Dyslexia, Dyscalculia, and all curious learners, NeuroPath blends granular local school district educational requirements with personalized interest worlds, regulation tools, global multi-language compatibility, pre-installed K-12 OER Commons curriculum materials with **Interactive Video and Audio Playback Modules**, an interactive **4-7-8 Breathing Exercise Visualizer**, an **Interactive Theme Preview Modal & Atmospheric Visualizer**, a library of **100 Adaptive Neuro-Themes with Automated Periodic Rotation**, a robust **Gemini Educational Chat Architecture with Multi-Tier Models (gemini-3.5-flash, gemini-3.1-flash-lite-preview, gemini-3.1-pro-preview)**, clean standardized UI chip/pill layouts, and speech-to-text voice assist.

---

## 🚀 Key Features in Recent Updates

### 📐 1. Standardized Sizing, Chip & Pill Formatting Polish (v1.14.00)
* **Eliminated Theme Category Pill Squeezing**: Redesigned the Active Theme Spotlight Card in the Child Profile Setup and Catalog items with stacked header rows and distinct badges, preventing vertical letter-by-letter wrapping when rendering long theme titles.
* **Streamlined Chat Header & Action Controls**: Re-architected the NeuroBuddy chat header with clean, unclipped Gemini model pills (`⚡ 3.5 Flash`, `🚀 Flash Lite`, `🧠 3.1 Pro`, `🔌 Offline`), balanced touch targets, and balanced session subtitles.
* **Ergonomic Educational Input Bar**: Optimized the bottom input field with adaptive single-to-multi-line height expansion, balanced 42dp action buttons, and concise placeholder prompts.
* **Enhanced Chip Padding & Font Metrics**: Standardized corner radii, text sizes, and padding across all follow-up question chips, starter suggestions, and cross-curricular subject badges.
* **WCAG Contrast Reinforcement**: Applied high-contrast dark typography and theme-accented metadata badges across all vibrant light cards.

### 🧠 2. Fixed Gemini AI Architecture & camelCase REST API Serialization (v1.13.00)
* **REST API camelCase Serialization**: Fixed Gemini API request/response serialization by aligning data classes (`generationConfig`, `systemInstruction`, `inlineData`, `topP`, `topK`, `thinkingConfig`, `speechConfig`, `voiceConfig`, `finishReason`) with the Google Generative Language v1beta API schema, eliminating HTTP 400 rejection errors.
* **Modern Multi-Tier Model Fleet**:
  * `gemini-3.5-flash`: Primary high-speed reasoning model for educational chat, adaptive hints, voice conversation turns, and curriculum downloads.
  * `gemini-3.1-flash-lite-preview`: Low-latency, quota-efficient free-tier fallback model.
  * `gemini-3.1-pro-preview`: Advanced multi-step STEM breakdown and deep concept tutor.
* **Multi-Source API Key Resolution**: Intelligently resolves credentials across AI Studio runtime secrets, profile configurations, and custom keys with seamless offline Socratic fallback.
* **Voice & Transcription Endpoints**: Restored real-time audio transcription and interactive live voice study sessions with proper model endpoint routing.

### 🎨 2. Interactive Theme Preview Modal & Palette Inspector (v1.12.00)
* **Theme Preview Modal**: In-depth modal dialog accessible directly from the Settings menu and Profile setup, allowing parents and learners to visualize how a selected theme's color palette, companion buddy, and background assets look before applying it globally.
* **Live Screen Simulation View**:
  * Real-time simulated Android screen frame with animated `Canvas` atmospheric mesh gradients and category-specific sensory background motifs (circuits, celestial rings, acoustic spectrums, ancient geometry).
  * Simulated Top Sensory Header Bar with dynamic XP and streak badges.
  * Companion Buddy speech bubble with authentic welcome dialogue and character badge.
  * Themed Active Quest / Lesson Card with subject-specific math/science challenges and themed action buttons.
* **WCAG & Dyslexia Color Palette Inspector**:
  * Visual color swatches for Primary Accent, Secondary Accent, Surface Background, and Card Container tokens with exact hex codes.
  * Contrast Readability scorecard (AAA certified) ensuring reduced blue-light eye strain during hyperfocus sessions.
  * Live interactive button, outlined action, and chip components rendered directly in the selected theme palette.
* **Curriculum & Companion Buddy Deep Dive**:
  * Cross-disciplinary subject connections (Math, Reading, Science, Social Studies) and sensory tactile interaction ideas.
  * Neurodivergent alignment badges (ADHD, Autism, Dyslexia, Sensory Processing).
* **Carousel & 100-Theme Browser**:
  * Smooth `< Previous` and `Next >` carousel buttons, full 100-theme search bar, category filter chips, rotation schedule configuration, and instant one-tap global application.

### 🎨 2. 100 Adaptive Neuro-Themes & Periodic Rotation Engine (v1.11.00)
* **100 Dynamic Neuro-Themes**: A vast library of 100 curated, multi-disciplinary theme worlds covering Pre-K through 12th Grade:
  * Ancient Civilizations (Ancient Egypt, Rome, Greece, Maya, Aztec, Mesopotamia, etc.)
  * Robotics & AI (Humanoid Robotics, Autonomous Rovers, Neural Networks, Cybernetics)
  * Mythological Creatures (Dragon Lore, Phoenix Guardians, Greek Mythology, Norse Legends)
  * Culinary Adventures (Master Chef Chemistry, World Street Food, Pastry Geometry)
  * Musical Journeys (Symphony Orchestra, Electronic Sound Synthesis, Global Folk)
  * Sports Superstars (Track & Field Physics, Basketball Statistics, Extreme Climbing)
  * Environmental Explorers (Amazon Rainforest, Deep Coral Reef, Arctic Glaciers)
  * Artistic Expression (Impressionist Light, Origami Geometry, Digital 3D Sculpting)
  * Transportation Tycoons (High-Speed Maglev, Aerospace Engineering, Steam Rail)
  * Spy Academy (Cryptography Secrets, Surveillance Physics, Forensic Science)
  * Deep Space, Medical Science, Architecture, Gaming & VR, and more!
* **Profile-Tailored AI Synthesis**: Themes are automatically recommended based on the whole of the child's diagnosis, strengths, struggles, age tier, and hyper-fixations.
* **Periodic Rotation Engine**: Parents/learners can choose between permanent theme world fixation or dynamic automatic rotation (Daily, Every 3 Days, Weekly, Bi-Weekly, Monthly) to ensure continuous neurodivergent engagement that grows with the child.
* **Full 100-Theme Browser Dialog**: Interactive in-setup catalog browser with search, category filtering, companion buddy stats, and cross-subject mapping (Math, Reading, Science, Social Studies).

### 🎬 2. OER Commons Video & Audio Playback Modules (v1.10.00)
* **Educational Video Player & Dynamic Canvas Visualizer**:
  * Rich multimedia video player hosted in a modal bottom sheet with animated Jetpack Compose `Canvas` visual simulations (including dynamic Plate Tectonics mantle convection, Quadratic Parabola trajectories, Atomic Bond electron clouds, and 10-Frame Counting grids).
  * Variable playback speed controls (`0.5x`, `0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`).
  * Continuous scrub slider with real-time timestamp display and 10-second skip forward/backward buttons.
* **Synchronized Closed Captions & Transcript Navigation**:
  * Live subtitle overlay matching active playback timestamps.
  * Interactive full transcript pane allowing children to tap any dialogue line to seek directly to that moment.
* **In-Video Socratic Checkpoints**:
  * Automated playback pausing at key conceptual timestamps to prompt learners with interactive multiple-choice Socratic check-ins.
  * Instant auditory and visual feedback with step-by-step conceptual explanations before resuming.
* **Auditory Curriculum & Podcast Player**:
  * Dedicated audio lecture mode with real-time audio waveform spectrum visualizer.
  * Synchronized Karaoke TTS read-aloud and key takeaway bullet points.
* **Seamless Course Integration**:
  * 1-Tap direct launch buttons in `TeachLessonScreen` discovery flow.
  * 1-Tap 🎬 Video and 🎧 Audio action chips within the `OerCuratedCollectionsBrowserSheet`.

### 🤖 3. Rebuilt Gemini Educational Chat Interface & Message History (v1.09.00)
* **Free Model Gemini Chatbot Integration**: Prioritizes `gemini-3.5-flash` and `gemini-3.1-flash-lite` free-tier models with full offline fallback Socratic tutoring for zero-cost educational access.
* **Full Access to OER Commons Curated Collections**: Seamless integration with [OER Commons Curated Collections](https://oercommons.org/curated-collections), featuring an interactive in-app browser sheet, direct web links, standard code alignments (CCSS, NGSS, C3), and 1-tap Socratic practice problem solving.
* **Personalized Explanation Modes**: Five one-tap configurable pedagogical styles (Step-by-Step, Simpler Analogy, Visual Breakdown, Deep Concept, Direct Answer).
* **Persistent Room Database Message History**: Multi-topic session creation, switching, deletion, offline storage, search, and bookmarking.

---

## 🏛️ About FourgeAI LABS

NeuroPath is created and maintained by **FourgeAI LABS**.
* Creator GitHub: [https://github.com/fourgeailabs](https://github.com/fourgeailabs)
* App Repository: [https://github.com/fourgeailabs/neuropath](https://github.com/fourgeailabs/neuropath)
* App Identifier: `com.fourgeailabs.neuropath`

---

## 📝 Changelog & Release History

### **v1.11.00** (Current Version)
* 🎨 **100 Adaptive Neuro-Themes**: Comprehensive library of 100 immersive theme worlds covering Pre-K through 12th Grade (Ancient Civilizations, Robotics & AI, Mythological Creatures, Culinary Adventures, Musical Journeys, Sports Superstars, Environmental Explorers, Artistic Expression, Transportation Tycoons, Spy Academy, Deep Space, Medical Science, Architecture, Gaming, etc.).
* 🔄 **Periodic Theme Rotation Engine**: Configurable rotation preferences (Permanent, Daily, Every 3 Days, Weekly, Bi-Weekly, Monthly) tailored to child diagnosis, strengths, struggles, and hyper-fixations.
* 📚 **100-Theme Catalog Browser**: Searchable dialog with category filters, companion buddy cards, and subject adaptations (Math, Reading, Science, Social Studies).
* 🎬 **OER Commons Video & Audio Playback**: Multimedia playback modules with interactive checkpoints, transcripts, speed toggles (0.75x - 1.5x), and full accessibility.

### **v1.10.00**
* 🎬 **OER Commons Video Player & Animated Visualizer**: Interactive educational video player with animated Canvas visual simulations, speed toggles (0.5x - 2.0x), and scrub bar.
* 🎧 **OER Auditory Curriculum & Story Podcast**: Audio lecture player with real-time audio waveform spectrum visualizer and synchronized Karaoke TTS read-aloud.
* 💡 **In-Video Socratic Checkpoints**: Video and audio playback automatically pauses at key timestamps for interactive conceptual check-ins and explanations.
* 🚀 **K-12 Course Integration**: 1-tap direct launch from `TeachLessonScreen` and the `OerCuratedCollectionsBrowserSheet` across all K-12 subjects.

### **v1.09.00**
* 🌐 **Full Access to OER Commons Curated Collections**: Seamless integration with [OER Commons Curated Collections](https://oercommons.org/curated-collections), featuring an interactive in-app browser sheet, direct web links, standard code alignments (CCSS, NGSS, C3), and 1-tap Socratic practice problem solving.
* 🤖 **Rebuilt Educational Chat Interface**: Overhauled Chat UI with full Gemini free-tier model prioritization (`gemini-3.5-flash` & `gemini-3.1-flash-lite`), clear model badges, and offline fallback.
* 🎓 **Personalized Explanation Engine**: Added 5 customizable explanation styles (Step-by-Step, Simpler Analogy, Visual Breakdown, Deep Concept, Direct Answer).
* 🗄️ **Room Chat Message History**: Complete local persistence with multi-topic session switching, history search, and bookmarks.

### **v1.08.00**
* 🤖 **Rebuilt Educational Chat Interface**: Overhauled Chat UI with full Gemini free-tier model prioritization (`gemini-3.5-flash`), clear model badges, and offline fallback.
* 🎓 **Personalized Explanation Engine**: Added 5 customizable explanation styles (Step-by-Step, Simpler Analogy, Visual Breakdown, Deep Concept, Direct Answer).
* 🗄️ **Room Chat Message History**: Complete local persistence with multi-topic session switching, history search, and bookmarks.
* ⚡ **Quick Transformation Tools**: Instant 'Explain Simpler', 'Breakdown Steps', TTS audio speech playback, and context-aware follow-up question chips.

### **v1.07.00**
* 🧘 **4-7-8 Breathing Exercise Visualizer Component**: Designed an interactive Canvas-based breathing visualizer with multi-layer animated lotus petals, fluid concentric waves, and cosmic orbital spheres.
* ⏱️ **Guided Rhythmic Feedback**: Real-time phase countdowns, animated segmented timeline bars, and continuous circular progress rings with phase-adaptive color schemes.
* 🌿 **Vagus Nerve Calm Index & Affirmations**: Live tracking of autonomic regulation with cycling sensory affirmations.
* 🎶 **Ambient Sound Integration**: Integrated soothing background soundscapes (Ocean Swells, Gentle Rain, Forest Breeze) directly into breathing sessions.


### **v1.06.00**
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
