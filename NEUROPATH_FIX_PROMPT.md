# NeuroPath — Full Code Fix Prompt (v2.05.00, 2026-09-19)

Copy everything below the line into the AI Studio builder chat and send it.
If the builder errors out on the whole thing, send it ONE BATCH at a time instead.

---

You are working on the NeuroPath Android app (Kotlin + Jetpack Compose, applicationId com.fourgeailabs.neuropath, versionName 2.05.00).

## Standing rules (apply to everything below, and to all future edits)

1. The runtime AI is **Meta Llama 3.2 3B Instruct only** (`meta-llama/Llama-3.2-3B-Instruct` via Hugging Face). The words "Gemini" and "Gemma" must not appear anywhere — not in code, comments, config files, or docs. "Lyria" (a Google model name) must not appear anywhere either — rename to neutral terms like "Soundscape".
2. Do not break anything that currently works. Every fix must keep existing working features intact.
3. Work through the batches **in order**. After each batch, verify the project still builds with zero errors before starting the next.
4. When you finish ALL batches, do a final full verification: the project must compile with zero errors and zero new warnings.

## Batch 1 — Critical: build, security, data loss, dishonest code

**1.1 Fix the KSP/Kotlin version mismatch (likely build-breaker).** `gradle/libs.versions.toml` declares `kotlin = "2.2.10"` but `googleDevtoolsKsp = "2.3.5"`. KSP must track the Kotlin compiler version. Align KSP to the 2.2.x line that matches Kotlin 2.2.10.

**1.2 Remove the last Gemini capability declaration.** `metadata.json` line 5 still contains `"majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]`. This is the single remaining Gemini reference in the whole project. Remove that capability (the runtime AI is Llama 3.2 via Hugging Face, not Gemini).

**1.3 Stop storing the parent PIN in plaintext.** `ChildProfileEntity.parentPin` is stored plaintext in Room and `updateParentPinForAll` overwrites it on every profile. Hash the PIN (SHA-256 with a per-install salt, at minimum) and compare hashes. Also fix `NeuroPathViewModel.verifyPin()` (~line 1748): it currently falls back to *any other profile's* PIN when the current profile has none — one child's PIN must never unlock another profile's gate. Decide deliberately: either one family-wide parent PIN or a strictly per-profile PIN, and implement exactly that.

**1.4 Move the Hugging Face token out of plaintext storage.** `ChildProfileEntity.customApiKey` holds the HF API token in plaintext inside the Room database. Move it to EncryptedSharedPreferences and never persist it in Room.

**1.5 Fix auto-backup leaking secrets.** `AndroidManifest.xml` sets `allowBackup="true"`, but `res/xml/data_extraction_rules.xml` and `res/xml/backup_rules.xml` are untouched sample templates — so the Room DB (parent PIN, API token, child profiles, chat history, diagnosis labels) and SharedPreferences back up to Google Drive effectively unencrypted. Write real backup rules that exclude the database file and any preferences holding secrets/tokens/PINs.

**1.6 Stop wiping children's data on upgrade.** `NeuroPathViewModel.kt` (~line 117) uses `.addMigrations(...).fallbackToDestructiveMigration()` with only migrations 9→10 and 10→11 present — any user on DB version 8 or below loses all local data (profiles, progress, chat history) silently on update. Remove the destructive fallback and add the missing migrations so upgrades preserve data. Also remove the no-op `MIGRATION_9_10` in `AppDatabase.kt` (it only runs `PRAGMA foreign_keys=ON`, which does nothing inside Room's migration transaction).

**1.7 Make release signing fail loudly.** `app/build.gradle.kts` (lines 33–56): if the upload keystore is absent, a "release" build silently signs with the debug key (`androiddebugkey` / "android"). A Play upload with the wrong key is rejected or creates a key-mismatch trap. Fail the build with a clear error when no upload key is configured instead of falling back.

**1.8 Fix dead voice input.** `LlamaClient.transcribeAudio()` (~line 141) is a stub that always returns `""`, so `NeuroPathViewModel.kt` (~line 1221) always takes the "Could not catch that clearly" path — voice input to the AI can never work even though recording succeeds. The app already has real speech-to-text via `SpeechManager` (Android `SpeechRecognizer`). Rewire voice input to use `SpeechManager`'s recognition result and delete the `transcribeAudio` stub.

**1.9 Make "live voice" honest.** `LlamaLiveApiClient.generateTurn()` ignores both `userVoiceAudio` and `apiKey`, never touches the network, and returns the offline canned reply with `audioBase64 = null` — while the class docstring claims "Real-time conversational audio" and it imports `WebSocket`/`WebSocketListener`/`Request`/`Response`/`JSONObject`/`Base64` that are never used. Remove all the dead WebSocket/OkHttp scaffolding and unused parameters, and rewrite the doc to describe what it actually does (produces a text turn; audio input/output are handled by `SpeechManager` speech recognition and TTS).

**1.10 Remove the fake AI-music feature and its Google branding.** `LlamaClient.generateSoundscapeMusic()` (~line 283) always returns `isSuccess = false, audioBase64 = null`, so AI soundscapes can never play — yet the UI promises them. And "Lyria" (Google DeepMind's music model) is all over the code: `audio/LyriaMusicPlayer.kt`, `LyriaMusicResult` in `network/LlamaModels.kt`, temp-file prefix `"lyria_soundscape_"`, `TopSensoryBar.kt:311` user-facing text promising "custom ambient audio on-demand using Google Lyria AI models (lyria-3-clip-preview & lyria-3-pro-preview)", and `ParentDashboardScreen.kt:2301` what's-new entry claiming "Google Lyria AI Music Generation". Do ALL of: (a) rename every `Lyria*` identifier/file to neutral `Soundscape*` names; (b) delete the false user-facing claims; (c) remove the dead ViewModel plumbing (`_isGeneratingLyriaMusic`, `_lyriaGeneratedStatus`, `generateLyriaSoundscape`, `toggleLyriaPlayPause`, `stopLyriaMusic`) and the dead status bar / bottom sheet UI in `TopSensoryBar.kt`; (d) make the soundscape button honestly play the real procedural sounds or remove it.

## Batch 2 — High: wrong behaviour, misleading UI, broken features

**2.1 Fix the streak counter.** `NeuroPathViewModel.checkAndUpdateStreak` (~lines 404–410): the `else` branch (last active date older than yesterday) does `currentStreakDays + 1` instead of resetting to 1 — a child who skips a week keeps growing their streak. Reset to 1 there. Also the `lastActiveDate == yesterday` branch sets a zero/negative streak to 2 instead of 1 — fix that too.

**2.2 Guard the lesson-score division.** `NeuroPathViewModel.finishLesson` (~line 1032): `correct.toFloat() / total.toFloat()` with `total == 0` yields `Int.MAX_VALUE`. Guard against zero questions.

**2.3 Fix the chat session Flow leak.** `NeuroPathViewModel.loadChatSession` (~line 1485) launches `repository.getChatMessagesForSessionFlow(...).collect {}` with no stored Job and never cancels it — every session load adds a permanent collector. Store the Job and cancel the previous one before collecting.

**2.4 Stop navigating during composition.** `TeachLessonScreen.kt:75-77` and `MasteryJourneyScreen.kt:76-78` call `viewModel.navigateTo(AppScreen.HOME)` directly in the composable body when `activeLesson == null`. Move these into `LaunchedEffect`.

**2.5 Fix stale profile-edit form state.** `ChildProfileSetupScreen.kt:114-135` uses unkeyed `remember { }` for every field, but the screen is embedded in `ProfileSelectionScreen.kt:79` and `ParentDashboardScreen.kt:162` with different `editingProfileId`s — switching profiles (or async profile arrival) shows the wrong data. Key the remembers on `editingProfileId`.

**2.6 Fix the Pop-It counter.** `NeuroPathViewModel.popBubble` increments `_totalPoppedCount` on every toggle, so un-popping inflates the count. Only count actual pops.

**2.7 Use collision-safe chat message IDs.** `sendChatMessage` / `sendLiveVoiceTurn` use `System.currentTimeMillis().toString()` with a `+1` hack — two messages in the same millisecond collide. Use UUIDs.

**2.8 Verify and fix the Hugging Face endpoint.** `LlamaClient.kt` builds `https://router.huggingface.co/hf-inference/models/meta-llama/Llama-3.2-3B-Instruct/` + `v1/chat/completions`. Check this against Hugging Face's official serverless inference docs and use the documented OpenAI-compatible chat-completions endpoint (model already goes in the request body). Also: `LlamaChatResponse.error` is modelled but never checked — check it, and surface API failures instead of silently falling back to the canned engine.

**2.9 Wire up the real on-device model.** In `LlamaClient.generateChatReply` (~line 197), `LLAMA_LOCAL` mode routes to the canned `generateLocalSocraticReply()` instead of `LlamaLocalManager.generateLlamaResponse()` (the real GGUF inference). Route `LLAMA_LOCAL` through the real local model.

**2.10 Make the model tiers honest.** `ChatModelMode` GENERAL/FAST/COMPLEX all hit the same model on the same endpoint — "Ultra-low latency" and "Deep STEM reasoning" labels have zero behavioural difference, and COMPLEX is marked `isFreeTier = false`. Either give each tier genuinely different parameters or collapse to a single honest "Llama 3.2 3B Cloud" mode.

**2.11 Fix the fake OER sync.** `OerCommonsCurriculumService.fetchAndParseOnlineCollection()` (~line 80) only GETs the base URL as a reachability check, then inserts the *preinstalled* catalog with REPLACE — wiping any previously synced online units. Rename it honestly and stop wiping synced data; implement real sync or remove the pretence.

**2.12 Stop silently mislabelling curriculum data.** `OerCurriculumEntity.toDomainModel()` maps unknown subjects to MATH and unknown grades to GRADE_1. Surface unknown values as unknown instead of wrongly-labelled content. Also fix the practice-problem round-trip: `fromDomainModel()` hardcodes `practiceProblemsJson = ""` and `toDomainModel()` returns `emptyList()` ("parsed when needed" — nothing ever parses it), so persisted practice problems are silently lost. Serialize them properly.

**2.13 Fix the non-deterministic chat summary query.** `AppDatabase.kt:33` selects bare `subjectTag` alongside `GROUP BY sessionId` — SQLite returns an arbitrary row's tag. Fix the query to be deterministic.

**2.14 Reconcile the application ID.** `app/build.gradle.kts:20` sets `applicationId = "com.fourgeailabs.neuropath"` (fourge-AI-labs). Verify this exact spelling against Play Console / OAuth / backend configs and make them agree. Also change `namespace = "com.example"` (`app/build.gradle.kts:17`) to the real package — shipping with the template namespace is sloppy.

**2.15 Remove the false Epidemic Sound attribution.** `CalmSoundManager.kt` synthesizes all sounds procedurally (`sin()`/noise) but credits `providerSource = "Epidemic Sound (https://www.epidemicsound.com/sound-effects/)"` and `PROVIDER_LABEL = "Sample Audio by Epidemic Sound"`. Remove the false licensing claim.

## Batch 3 — Medium: correctness, privacy polish, data integrity

**3.1 Fix the repository race conditions.** `NeuroPathRepository.awardStarsAndGems`, `unlockItem`, and `recordLessonCompletion` do read-modify-write with no `@Transaction` — concurrent calls can lose stars/gems or double-spend. Add `@Transaction`, and make `unlockItem` check the item isn't already unlocked before charging.

**3.2 Stop presetting clinical labels on fresh installs.** `NeuroPathRepository.getOrCreateProfile()` (~lines 88–116) fabricates a profile with `neurodivergentTypesCsv = "ADHD,AUTISM_ASD"` plus preset struggles/strengths/interests and California/LAUSD defaults before the user enters anything. A new install must not carry fake diagnosis labels. Also `NeuroPathViewModel.kt` (~lines 170–195) ships a phantom empty-named fallback profile hardcoded to California/Los Angeles/LAUSD — remove it.

**3.3 Fix `LearnerPersonalizationEngine`.** (a) `recordAnswer()` only ever *adds* topics to the missed list — later correct answers never remove them, so "weaknesses" persist forever; remove topics on correct answers. (b) `recordAnswer()` does unsynchronized read-modify-write on SharedPreferences — synchronize it. (c) `buildPrompt()`/`topicMasterySummary()` interpolate stored question text verbatim into the AI system prompt — sanitize/normalize the text (strip newlines/instruction-like content) before storing and interpolating.

**3.4 Clean up `LocationComplianceHelper`.** (a) Delete the hardcoded developer ZIPs in `mapKnownUsZip` (85374/85378/85379/85387/85388 → Arizona/Surprise) that take precedence over the geocoder, and the unreachable inner `when` branch in `resolvePostalOrZipCode()` that can never execute. (b) `isVerified = true` is set unconditionally in `detectAndVerifyHomeCountry()`, even on the SIM/locale fallback — make it reflect reality. (c) Fix misrouting: short-prefix UK checks (e.g. `startsWith("B")`) run before the Canada branches and can misroute Canadian postcodes; arbitrary fallbacks disagree (unrecognized US ZIP → California/LA, but ultimate fallback → UK/London). Make fallbacks consistent with the app's defaults. (d) `CurriculumResolver.normalizeCountry` maps "CA" to Canada — "CA" is also California's abbreviation; disambiguate.

**3.5 Fix the stale User-Agent.** `OerCommonsCurriculumService.kt:89` hardcodes `"NeuroPath-K12-Educational-App/2.00.00"` while `versionName` is `2.05.00`. Use `BuildConfig.VERSION_NAME` like `LlamaLocalManager.kt:114` and `LlamaAccelerator.kt:97` already do.

**3.6 Fix version drift in release notes.** `WhatsNewDialog.kt:60` marks v2.04.00 as current while `versionName` is 2.05.00 (and `ParentDashboardScreen.kt:2145` lists 2.05.00 correctly). Make them agree. Also `.github/workflows/build.yml` uses `v2.04.00` as the fallback release tag — update to 2.05.00.

**3.7 Fix `LlamaLocalManager` issues.** (a) `HUGGINGFACE_REPO_URL` (line 32) points at the non-Instruct `meta-llama/Llama-3.2-3B` repo — correct it to the Instruct repo used everywhere else. (b) `deleteLlamaModel()` leaves the downloaded LiteRT `.litertlm` file behind — delete it too. (c) `generateLlamaResponse` runs heavy native inference on `Dispatchers.Default` — use a dedicated dispatcher. (d) The LiteRT accelerator path gets a plain-text prompt while the GGUF path gets the proper Llama-3 chat template — give the accelerator the chat template too. (e) `MIN_MODEL_BYTES = 450MB` is a weak sanity floor for a ~2GB model — raise it.

**3.8 Deduplicate the SoC/hardware mapping.** `LlamaAccelerator.selectedModelFilename()` and `LocalAiHardwareManager.potentialNpuBackend()` duplicate the same SoC→model/backend mapping, and Tensor G5/G6 detection uses fragile `contains("g5")` substring matching. Single-source it and make detection robust. Also remove the hardcoded-`false` dead fields `LocalAiHardwareManager.gpuRuntimeAvailable` and `LlamaLocalManager.checkDeviceCompatibility().hasGpuAcceleration`.

**3.9 Fix `AudioRecorderHelper` threading.** `isRecording` is written on the calling thread and read on the recording thread without `@Volatile` — add it. Remove unused imports (`File`, `FileOutputStream`, `RandomAccessFile`). `join(500)` can proceed before the thread exits — join properly.

**3.10 Fix `SpeechManager` issues.** Remove the private `isNull_or_blank()` that duplicates stdlib `isNullOrBlank()`. `speak()` silently no-ops when TTS init failed — surface the failure. `stop()` must clear `_currentSpokenTranscription`/`_speechTextResult`. Fix partial STT results overwriting the final-result flow.

**3.11 Add the missing `else` in `MainActivity`.** The `when (currentScreen)` has no `else` — a new `AppScreen` value renders a blank screen silently. Add an `else` that logs and falls back to HOME.

**3.12 Clean up dead config.** (a) The `com.google.gms.google-services` plugin is applied with zero Firebase dependencies and no `google-services.json` (only the `.example`) — remove the dead plugin (keep the WARN strategy removal consistent). (b) Delete the leftover one-off script `patch_dict.py` at the repo root. (c) `System.getenv("HF_TOKEN")` etc. in `LlamaClient.getApiKey()` can never return anything on Android — remove the dead env lookups (keep the `BuildConfig` path). (d) Make `HttpLoggingInterceptor` debug-only instead of unconditional `Level.BASIC`.

**3.13 Fix `LlamaClient.parseAndEvaluateMath` edge cases.** Division by zero silently yields `0L`; add negative-number support.

## Batch 4 — Cleanup: dead code, modernisation, accessibility, i18n

**4.1 Delete dead theme code.** `ui/theme/Theme.kt` (`MyApplicationTheme`, line 34) is never called — `MainActivity` uses `getThemeColorScheme` + `getDyslexiaTypography` directly; delete the file and the `Purple80/Purple40/...` vals in `Color.kt` that only it uses. Delete the never-called `WorldTheme` overload of `getThemeColorScheme` in `NeuroPathTheme.kt` (a ~60-line duplicate of the contrast-mode `when` block). Remove the decorative `darkTheme: Boolean = false` parameter both overloads declare but never read — or implement dark mode properly. Delete unused `PastelLavender/PastelTeal/PastelPeach/PastelSky` vals.

**4.2 Modernise enum access.** Replace deprecated `.values()` with `.entries` (`AgeGroupTier`, `GradeLevel`, `ChatModelMode`, `WorldTheme`).

**4.3 Accessibility for the actual audience (neurodiverse kids).** (a) `BreathingGuideScreen.kt:209` hardcodes `isReducedMotion = false` and the `ambientPulse` animation runs unconditionally — honour the system reduced-motion setting and add a user toggle. (b) Many touch targets are below the 48dp minimum (`ChildProfileSetupScreen` 24dp/36dp icon buttons, `CreativeStudioScreen` 28dp/36dp, `BreathingGuideScreen` 34dp/44dp) — enlarge them.

**4.4 Internationalisation.** ~213 hardcoded English `Text("...")` literals across screens/components with zero `stringResource` usage, despite shipped `values-de/es/fr/zh` and the `AppLanguage` system. Move user-visible strings into string resources (start with the most-used screens: Home, Chat, TeachLesson, MasteryJourney).

**4.5 Minor fixes.** (a) `navigateTo(BREATHING_GUIDE)` restarts the breathing coroutine even when already on that screen, resetting progress — skip when already there. (b) The breathing `while(true)` loop writes a Room row every 19 seconds for the whole session — batch or throttle the logging. (c) Remove the duplicated "Theme world:"/"District context:" lines in `sendChatMessage`'s `LLAMA_LOCAL` system prompt. (d) `appendPinDigit(digit: String)` accepts arbitrary strings — validate digits. (e) `CreativeStudioScreen.kt:120` `storyPrompts.first()` crashes if the list were ever empty — add a safe fallback.

Final step: build the whole project and confirm **zero errors**. Report anything you could not fix and why.
