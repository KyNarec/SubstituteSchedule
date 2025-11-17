package org.substitute.schedule.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KClass
import kotlin.reflect.cast

class AndroidSecureStorage(context: Context) : SecureStorage {
    private val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()

        // Emit the change globally
        SecureStorageEvents.emitStringUpdate(key, value)
    }

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

    override fun observeString(key: String): Flow<String?> = flow {
        // Emit current value first
        emit(getString(key) ?: "")

        SecureStorageEvents.stringUpdates.collect { (k, v) ->
            if (k == key) emit(v)
        }
    }

    override fun <T : Enum<T>> putEnum(key: String, value: T) {
        prefs.edit().putString(key, value.name).apply()
        SecureStorageEvents.emitEnumUpdate(key, value)
    }

    override fun <T : Enum<T>> getEnum(key: String, enumClass: KClass<T>): T? {
        val name = prefs.getString(key, null)
        return name?.let {
            runCatching { java.lang.Enum.valueOf(enumClass.java, it) }.getOrNull()
        }
    }

    override fun <T : Enum<T>> observeEnum(key: String, enumClass: KClass<T>): Flow<T?> = flow {
        val initial = getEnum(key, enumClass)
//        println("App(): observeEnum initial for $key: $initial")
        emit(initial)

        SecureStorageEvents.enumUpdates.collect { (k, v) ->
            if (k == key && enumClass.isInstance(v)) {
                val casted = enumClass.cast(v)
//                println("App(): observeEnum update for $key: $casted")
                emit(casted)
            }
        }
    }

    override fun observeContains(key: String): Flow<Boolean> = flow {
        emit(prefs.contains(key))
        SecureStorageEvents.booleanUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
    }

}