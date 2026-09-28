# NeuroPath release keep rules.
# Audited against the actual codebase (2026-09-27):
#  - JNI bridge: dev.ffmpegkit.llama.Llama (llama-android 0.1.1, used via LlamaLocalManager.RealLlamaRuntime)
#  - Room: AppDatabase (schema v15, migrations 9->15), entities in data.local.entity, DAOs in data.local
#  - Moshi: @JsonClass(generateAdapter=true) models (LessonModels.kt, LlamaModels.kt);
#           OfflinePackManager uses plain Moshi.Builder (codegen adapters);
#           LlamaClient adds KotlinJsonAdapterFactory (reflection path)
#  - Retrofit: LlamaApiService (network/LlamaClient.kt)
#
# WebView/JS-interface block intentionally left out: the app has no WebView.

# ── JNI / llama.cpp native bridge ───────────────────────────────
# Native method names must match the JNI-registered names exactly: keep the
# whole bridge package unobfuscated and preserve every native method.
-keep class dev.ffmpegkit.llama.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# ── Room (schema v15, migration 14->15) ─────────────────────────
# Database class, entities, DAOs, top-level migrations, and the @Query
# projection class ChatSessionSummary (mapped by column name at runtime).
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.fourgeailabs.neuropath.data.local.AppDatabase { *; }
-keep class com.fourgeailabs.neuropath.data.local.AppDatabaseKt { *; }
-keep class com.fourgeailabs.neuropath.data.local.ChatSessionSummary { *; }
-keepclassmembers class * {
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
    @androidx.room.Transaction <methods>;
}
# Room resolves entity fields via the generated _Impl classes and reflection
# on constructors; keep entity constructors and fields intact.
-keepclassmembers @androidx.room.Entity class * {
    <init>(...);
    <fields>;
}

# ── Moshi (offline packs + network) ────────────────────────────
# Keep generated *JsonAdapter classes and every @JsonClass-annotated model
# (FullLesson, LlamaChatRequest/Response, LessonContext, ...).
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *JsonAdapter { *; }
-keepclasseswithmembernames class * {
    @com.squareup.moshi.Json <fields>;
}
-keepclasseswithmembernames class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
# LlamaClient adds KotlinJsonAdapterFactory (reflection fallback for the
# generated adapters); keep the Kotlin metadata it reflects on.
-keepclassmembers class kotlin.Metadata { *; }
-keepclassmembers class * {
    @com.squareup.moshi.JsonQualifier <annotations>;
}

# ── Retrofit / OkHttp (LlamaApiService) ─────────────────────────
# Keep the service interface and every retrofit-annotated method signature.
-keep interface com.fourgeailabs.neuropath.network.LlamaApiService { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature, InnerClasses, EnclosingMethod

# ── Debug aids ──────────────────────────────────────────────────
# Preserve line numbers so release crash reports stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
