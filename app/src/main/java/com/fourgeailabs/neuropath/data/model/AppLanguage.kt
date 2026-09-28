package com.fourgeailabs.neuropath.data.model

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val locale: Locale
) {
    ENGLISH_US("en-US", "English (American)", "English (US)", "🇺🇸", Locale.US),
    ENGLISH_UK("en-GB", "English (British)", "English (UK)", "🇬🇧", Locale.UK),
    SPANISH("es", "Spanish", "Español", "🇪🇸", Locale.Builder().setLanguage("es").setRegion("ES").build()),
    FRENCH("fr", "French", "Français", "🇫🇷", Locale.FRANCE),
    GERMAN("de", "German", "Deutsch", "🇩🇪", Locale.GERMANY),
    MANDARIN("zh", "Chinese (Mandarin)", "中文 (普通话)", "🇨🇳", Locale.CHINA),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵", Locale.JAPAN),
    PORTUGUESE("pt", "Portuguese", "Português", "🇧🇷", Locale.Builder().setLanguage("pt").setRegion("BR").build()),
    HINDI("hi", "Hindi", "हिंदी", "🇮🇳", Locale.Builder().setLanguage("hi").setRegion("IN").build()),
    ARABIC("ar", "Arabic", "العربية", "🇸🇦", Locale.Builder().setLanguage("ar").build()),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹", Locale.ITALY),
    RUSSIAN("ru", "Russian", "Русский", "🇷🇺", Locale.Builder().setLanguage("ru").build()),
    KOREAN("ko", "Korean", "한국어", "🇰🇷", Locale.KOREA),
    TURKISH("tr", "Turkish", "Türkçe", "🇹🇷", Locale.Builder().setLanguage("tr").build()),
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt", "🇻🇳", Locale.Builder().setLanguage("vi").build()),
    POLISH("pl", "Polish", "Polski", "🇵🇱", Locale.Builder().setLanguage("pl").build()),
    DUTCH("nl", "Dutch", "Nederlands", "🇳🇱", Locale.Builder().setLanguage("nl").build()),
    THAI("th", "Thai", "ไทย", "🇹🇭", Locale.Builder().setLanguage("th").build()),
    INDONESIAN("id", "Indonesian", "Bahasa Indonesia", "🇮🇩", Locale.Builder().setLanguage("id").build()),
    SWEDISH("sv", "Swedish", "Svenska", "🇸🇪", Locale.Builder().setLanguage("sv").build()),
    GREEK("el", "Greek", "Ελληνικά", "🇬🇷", Locale.Builder().setLanguage("el").build());

    companion object {
        fun fromCode(code: String): AppLanguage {
            val clean = code.trim().lowercase()
            return when {
                clean == "en-gb" || clean == "gb" || clean == "uk" -> ENGLISH_UK
                clean == "en-us" || clean == "en" || clean == "us" -> ENGLISH_US
                else -> values().find { it.code.equals(code, ignoreCase = true) || it.code.startsWith(code, ignoreCase = true) } ?: ENGLISH_US
            }
        }
    }
}

fun tr(key: String, languageCode: String): String = AppLanguageDictionary.getString(key, languageCode)

object AppLanguageDictionary {
    // Per-child offline-pack strings are kept in OfflinePackStrings (100% per-language
    // coverage) and merged here so tr()/t() resolve them for every language.
    private val enUs = AppLanguageDictionariesEn.enUs + OfflinePackStrings.enUs
    private val enGb = AppLanguageDictionariesEn.enGb + OfflinePackStrings.enGb
    private val es = AppLanguageDictionariesEs.es + OfflinePackStrings.es
    private val fr = AppLanguageDictionariesFr.fr + OfflinePackStrings.fr
    private val de = AppLanguageDictionariesDe.de + OfflinePackStrings.de
    private val itMap = AppLanguageDictionariesIt.it + OfflinePackStrings.it
    private val pt = AppLanguageDictionariesPt.pt + OfflinePackStrings.pt
    private val nl = AppLanguageDictionariesNl.nl + OfflinePackStrings.nl
    private val sv = AppLanguageDictionariesSv.sv + OfflinePackStrings.sv

    private val zh = AppLanguageDictionariesZh.zh + OfflinePackStrings.zh
    private val ja = AppLanguageDictionariesJa.ja + OfflinePackStrings.ja
    private val ko = AppLanguageDictionariesKo.ko + OfflinePackStrings.ko
    private val vi = AppLanguageDictionariesVi.vi + OfflinePackStrings.vi
    private val th = AppLanguageDictionariesTh.th + OfflinePackStrings.th
    private val id = AppLanguageDictionariesId.id + OfflinePackStrings.id
    private val hi = AppLanguageDictionariesHi.hi + OfflinePackStrings.hi
    private val ar = AppLanguageDictionariesAr.ar + OfflinePackStrings.ar

    private val ru = AppLanguageDictionariesRu.ru + OfflinePackStrings.ru
    private val trMap = AppLanguageDictionariesTr.tr + OfflinePackStrings.tr
    private val pl = AppLanguageDictionariesPl.pl + OfflinePackStrings.pl
    private val el = AppLanguageDictionariesEl.el + OfflinePackStrings.el

    private val translations: Map<String, Map<String, String>> = mapOf(
        "en-us" to enUs,
        "en" to enUs,
        "en-gb" to enGb,
        "es" to es,
        "fr" to fr,
        "de" to de,
        "it" to itMap,
        "pt" to pt,
        "nl" to nl,
        "sv" to sv,
        "zh" to zh,
        "ja" to ja,
        "ko" to ko,
        "vi" to vi,
        "th" to th,
        "id" to id,
        "hi" to hi,
        "ar" to ar,
        "ru" to ru,
        "tr" to trMap,
        "pl" to pl,
        "el" to el
    )

    fun getString(key: String, languageCode: String): String {
        val cleanLang = languageCode.trim().lowercase()
        val langMap = translations[cleanLang] 
            ?: translations[cleanLang.take(2)] 
            ?: enUs
        return langMap[key] ?: enUs[key] ?: key
    }

    fun getLanguageMap(languageCode: String): Map<String, String> {
        val cleanLang = languageCode.trim().lowercase()
        return translations[cleanLang] ?: translations[cleanLang.take(2)] ?: enUs
    }

    fun getAllKeys(): Set<String> = enUs.keys
}
