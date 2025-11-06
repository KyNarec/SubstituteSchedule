package org.substitute.schedule.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.substitute.schedule.SecureStorageEvents

class AndroidSecureStorage(context: Context) : SecureStorage {
    private val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun remove(key: String) = prefs.edit().remove(key).apply()

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()

        // Emit the change globally
        SecureStorageEvents.emitBooleanUpdate(key, value)
    }

    override fun getBoolean(key: String): Boolean = prefs.getBoolean(key, false)

    override fun observeBoolean(key: String): Flow<Boolean> = flow {
        // Emit current value first
        emit(getBoolean(key))

        // Then emit updates for this specific key from the global flow
        SecureStorageEvents.booleanUpdates.collect { (k, v) ->
            if (k == key) emit(v)
        }
    }
}