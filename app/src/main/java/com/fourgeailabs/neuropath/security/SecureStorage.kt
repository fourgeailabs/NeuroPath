package com.fourgeailabs.neuropath.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * App-level secure storage for secrets: the parent gate PIN and the Hugging Face API token.
 *
 * Both are stored outside the Room database (never in
 * [com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity]):
 * - The parent PIN is kept as a PBKDF2-HMAC hash with a fresh 128-bit salt per set, so
 *   identical PINs hash differently and rainbow tables do not apply. A 4-digit PIN has only
 *   10,000 possibilities, so the high iteration count is what makes offline brute force
 *   expensive rather than instant.
 * - The HF token is stored in EncryptedSharedPreferences (AES-256, key in AndroidKeyStore).
 *
 * Defined semantics: there is exactly ONE parent PIN and ONE HF token per app install.
 * They are not per child profile, and verification never falls back to "any profile that
 * happens to have one set".
 *
 * Fail-closed behaviour: if the AndroidKeyStore-backed encrypted store cannot be created
 * (broken keystore on some devices), there is deliberately NO plaintext fallback. Getters
 * return empty/false and setters return false so callers degrade honestly — the parent gate
 * stays shut rather than silently storing secrets in the clear.
 */
class SecureStorage private constructor(context: Context) {

    private val prefs: SharedPreferences?

    /** False when encrypted storage could not be initialised; all setters then fail. */
    val isEncryptionAvailable: Boolean

    init {
        var loaded: SharedPreferences? = null
        try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            loaded = EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences unavailable; secure storage disabled", e)
        }
        prefs = loaded
        isEncryptionAvailable = loaded != null
    }

    // ------------------------------------------------------------------ parent PIN ---

    fun hasParentPin(): Boolean =
        prefs?.contains(KEY_PIN_SALT) == true && prefs?.contains(KEY_PIN_HASH) == true

    /**
     * Stores [pin] as a PBKDF2 hash with a fresh random salt. Only accepts 4-digit numeric
     * PINs; anything else throws. Returns false when encrypted storage is unavailable.
     * Runs the key derivation off the main thread.
     */
    suspend fun setParentPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        require(pin.matches(PIN_REGEX)) { "Parent PIN must be exactly 4 digits" }
        val p = prefs ?: return@withContext false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val algorithm = pbkdf2Algorithm()
        val hash = pbkdf2(pin, salt, algorithm) ?: return@withContext false
        p.edit()
            .putString(KEY_PIN_SALT, salt.toHex())
            .putString(KEY_PIN_HASH, hash.toHex())
            .putString(KEY_PIN_ALGO, algorithm)
            .apply()
        true
    }

    /**
     * Verifies [pin] against the stored PBKDF2 hash in constant time. Returns false when no
     * PIN is stored, the PIN is malformed, or encrypted storage is unavailable.
     * Runs the key derivation off the main thread.
     */
    suspend fun verifyParentPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        val p = prefs ?: return@withContext false
        val saltHex = p.getString(KEY_PIN_SALT, null) ?: return@withContext false
        val expectedHex = p.getString(KEY_PIN_HASH, null) ?: return@withContext false
        val algorithm = p.getString(KEY_PIN_ALGO, null) ?: return@withContext false
        if (!pin.matches(PIN_REGEX)) return@withContext false
        val salt = saltHex.fromHex() ?: return@withContext false
        val expected = expectedHex.fromHex() ?: return@withContext false
        val actual = pbkdf2(pin, salt, algorithm) ?: return@withContext false
        MessageDigest.isEqual(expected, actual)
    }

    fun clearParentPin() {
        prefs?.edit()
            ?.remove(KEY_PIN_SALT)
            ?.remove(KEY_PIN_HASH)
            ?.remove(KEY_PIN_ALGO)
            ?.apply()
    }

    // ------------------------------------------------------------------ HF token ---

    fun getHfToken(): String = prefs?.getString(KEY_HF_TOKEN, "").orEmpty()

    /** Returns false when encrypted storage is unavailable; the token is then not stored. */
    fun setHfToken(token: String): Boolean {
        val p = prefs ?: return false
        p.edit().putString(KEY_HF_TOKEN, token).apply()
        return true
    }

    fun clearHfToken() {
        prefs?.edit()?.remove(KEY_HF_TOKEN)?.apply()
    }

    // ------------------------------------------------------------------ internals ---

    private fun pbkdf2Algorithm(): String =
        if (runCatching { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256") }.isSuccess)
            "PBKDF2WithHmacSHA256"
        else
            "PBKDF2WithHmacSHA1" // pre-API-26 fallback; iteration count is what matters here

    private fun pbkdf2(pin: String, salt: ByteArray, algorithm: String): ByteArray? {
        val chars = pin.toCharArray()
        return try {
            val spec = PBEKeySpec(chars, salt, PBKDF2_ITERATIONS, 256)
            runCatching { SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded }
                .getOrNull()
        } finally {
            chars.fill('\u0000')
        }
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
        private const val KEY_PIN_ALGO = "parent_pin_algo"
        private const val KEY_HF_TOKEN = "hf_token"
        private val PIN_REGEX = Regex("^\\d{4}$")

        /**
         * PBKDF2 iteration count. A 4-digit PIN has 10,000 candidates; this count makes each
         * guess cost ~a second of CPU on a modern phone, turning brute force from instant
         * into days. Verification runs on Dispatchers.Default so the UI never janks.
         */
        private const val PBKDF2_ITERATIONS = 310_000

        @Volatile
        private var instance: SecureStorage? = null

        fun getInstance(context: Context): SecureStorage =
            instance ?: synchronized(this) {
                instance ?: SecureStorage(context.applicationContext).also { instance = it }
            }
    }
}
