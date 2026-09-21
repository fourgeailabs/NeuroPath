package com.fourgeailabs.neuropath.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * App-level secure storage for secrets: the parent gate PIN and the Hugging Face API token.
 *
 * Both are stored outside the Room database (never in [com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity]):
 * - The parent PIN is kept as a salted SHA-256 hash. A fresh 128-bit salt is generated on
 *   every set, so identical PINs hash differently and rainbow tables do not apply.
 * - The HF token is stored in EncryptedSharedPreferences (AES-256, key in AndroidKeyStore).
 *
 * Defined semantics: there is exactly ONE parent PIN and ONE HF token per app install.
 * They are not per child profile, and verification never falls back to "any profile that
 * happens to have one set".
 *
 * If the AndroidKeyStore-backed encrypted store cannot be created (broken keystore on some
 * devices), this falls back to a private-mode SharedPreferences file with a warning. The PIN
 * remains a salted hash either way; only the at-rest encryption layer is degraded.
 */
class SecureStorage private constructor(context: Context) {

    private val prefs: SharedPreferences

    init {
        prefs = runCatching {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse { e ->
            Log.w(TAG, "EncryptedSharedPreferences unavailable, using private prefs fallback", e)
            context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // ------------------------------------------------------------------ parent PIN ---

    fun hasParentPin(): Boolean =
        prefs.contains(KEY_PIN_SALT) && prefs.contains(KEY_PIN_HASH)

    /**
     * Stores [pin] as a salted SHA-256 hash with a fresh random salt.
     * Only accepts 4-digit numeric PINs; anything else is rejected loudly.
     */
    fun setParentPin(pin: String) {
        require(pin.matches(Regex("^\\d{4}$"))) { "Parent PIN must be exactly 4 digits" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt.toHex())
            .putString(KEY_PIN_HASH, hash.toHex())
            .apply()
    }

    fun verifyParentPin(pin: String): Boolean {
        val saltHex = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val expectedHex = prefs.getString(KEY_PIN_HASH, null) ?: return false
        if (!pin.matches(Regex("^\\d{4}$"))) return false
        val salt = saltHex.fromHex() ?: return false
        val expected = expectedHex.fromHex() ?: return false
        val actual = hashPin(pin, salt)
        return MessageDigest.isEqual(expected, actual)
    }

    fun clearParentPin() {
        prefs.edit().remove(KEY_PIN_SALT).remove(KEY_PIN_HASH).apply()
    }

    // ------------------------------------------------------------------ HF token ---

    fun getHfToken(): String = prefs.getString(KEY_HF_TOKEN, "").orEmpty()

    fun setHfToken(token: String) {
        prefs.edit().putString(KEY_HF_TOKEN, token).apply()
    }

    fun clearHfToken() {
        prefs.edit().remove(KEY_HF_TOKEN).apply()
    }

    // ------------------------------------------------------------------ internals ---

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(pin.toByteArray(Charsets.UTF_8))
        return digest.digest()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray? = runCatching {
        require(length % 2 == 0)
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }.getOrNull()

    companion object {
        const val SECURE_PREFS_NAME = "neuropath_secure_prefs"
        private const val TAG = "SecureStorage"
        private const val KEY_PIN_SALT = "parent_pin_salt"
        private const val KEY_PIN_HASH = "parent_pin_hash"
        private const val KEY_HF_TOKEN = "hf_token"

        @Volatile
        private var instance: SecureStorage? = null

        fun getInstance(context: Context): SecureStorage =
            instance ?: synchronized(this) {
                instance ?: SecureStorage(context.applicationContext).also { instance = it }
            }
    }
}
