package com.example.data.model

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
    SPANISH("es", "Spanish", "Español", "🇪🇸", Locale("es", "ES")),
    FRENCH("fr", "French", "Français", "🇫🇷", Locale.FRANCE),
    GERMAN("de", "German", "Deutsch", "🇩🇪", Locale.GERMANY),
    MANDARIN("zh", "Chinese (Mandarin)", "中文 (普通话)", "🇨🇳", Locale.CHINA),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵", Locale.JAPAN),
    PORTUGUESE("pt", "Portuguese", "Português", "🇧🇷", Locale("pt", "BR")),
    HINDI("hi", "Hindi", "हिंदी", "🇮🇳", Locale("hi", "IN")),
    ARABIC("ar", "Arabic", "العربية", "🇸🇦", Locale("ar")),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹", Locale.ITALY),
    RUSSIAN("ru", "Russian", "Русский", "🇷🇺", Locale("ru")),
    KOREAN("ko", "Korean", "한국어", "🇰🇷", Locale.KOREA),
    TURKISH("tr", "Turkish", "Türkçe", "🇹🇷", Locale("tr")),
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt", "🇻🇳", Locale("vi")),
    POLISH("pl", "Polish", "Polski", "🇵🇱", Locale("pl")),
    DUTCH("nl", "Dutch", "Nederlands", "🇳🇱", Locale("nl")),
    THAI("th", "Thai", "ไทย", "🇹🇭", Locale("th")),
    INDONESIAN("id", "Indonesian", "Bahasa Indonesia", "🇮🇩", Locale("id")),
    SWEDISH("sv", "Swedish", "Svenska", "🇸🇪", Locale("sv")),
    GREEK("el", "Greek", "Ελληνικά", "🇬🇷", Locale("el"));

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
    private val enUs = AppLanguageDictionariesPart1.enUs
    private val enGb = AppLanguageDictionariesPart1.enGb
    private val es = AppLanguageDictionariesPart1.es
    private val fr = AppLanguageDictionariesPart1.fr
    private val de = AppLanguageDictionariesPart1.de
    private val itMap = AppLanguageDictionariesPart1.it
    private val pt = AppLanguageDictionariesPart1.pt
    private val nl = AppLanguageDictionariesPart1.nl
    private val sv = AppLanguageDictionariesPart1.sv

    private val zh = AppLanguageDictionariesPart2.zh
    private val ja = AppLanguageDictionariesPart2.ja
    private val ko = AppLanguageDictionariesPart2.ko
    private val vi = AppLanguageDictionariesPart2.vi
    private val th = AppLanguageDictionariesPart2.th
    private val id = AppLanguageDictionariesPart2.id
    private val hi = AppLanguageDictionariesPart2.hi
    private val ar = AppLanguageDictionariesPart2.ar

    private val ru = AppLanguageDictionariesPart3.ru
    private val trMap = AppLanguageDictionariesPart3.tr
    private val pl = AppLanguageDictionariesPart3.pl
    private val el = AppLanguageDictionariesPart3.el

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
