package org.substitute.schedule.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

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
}