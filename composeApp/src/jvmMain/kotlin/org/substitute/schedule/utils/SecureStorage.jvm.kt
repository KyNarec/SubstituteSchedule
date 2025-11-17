package org.substitute.schedule.utils

import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.substitute.schedule.utils.SecureStorageEvents
import kotlin.reflect.KClass
import kotlin.reflect.cast

class DesktopSecureStorage : SecureStorage {
    private val file = File(System.getProperty("user.home"), ".securestore")
    private val key = SecretKeySpec("your-32-byte-key-goes-here!".toByteArray(), "AES")
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class StorageData(val values: Map<String, String> = emptyMap())

    private fun readAllData(): StorageData {
        if (!file.exists()) return StorageData()
        return try {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decrypted = cipher.doFinal(file.readBytes())
            json.decodeFromString<StorageData>(String(decrypted))
        } catch (e: Exception) {
            StorageData()
        }
    }

    private fun writeAllData(data: StorageData) {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val jsonString = json.encodeToString(data)
        val encrypted = cipher.doFinal(jsonString.toByteArray())
        file.writeBytes(encrypted)
    }

    override fun putString(keyName: String, value: String) {
        val data = readAllData()
        val updatedValues = data.values.toMutableMap()
        updatedValues[keyName] = value
        writeAllData(StorageData(updatedValues))

        // Emit the change globally
        SecureStorageEvents.emitStringUpdate(keyName, value)
    }

    override fun getString(keyName: String): String? {
        return readAllData().values[keyName]
    }

    override fun putBoolean(keyName: String, value: Boolean) {
        val data = readAllData()
        val updatedValues = data.values.toMutableMap()
        updatedValues[keyName] = value.toString()
        writeAllData(StorageData(updatedValues))

        // Emit the change globally
        SecureStorageEvents.emitBooleanUpdate(keyName, value)
    }

    override fun getBoolean(keyName: String): Boolean {
        return readAllData().values[keyName]?.toBoolean() ?: false
    }

    override fun observeBoolean(key: String): Flow<Boolean> = flow {
        // Emit current value first
        emit(getBoolean(key))

        // Then emit updates for this specific key from the global flow
        SecureStorageEvents.booleanUpdates.collect { (k, v) ->
            if (k == key) emit(v)
        }
    }

    override fun observeString(key: String): Flow<String> = flow {
        // Emit current value first
        emit(getString(key) ?: "")

        SecureStorageEvents.stringUpdates.collect { (k, v) ->
            if (k == key) emit(v)
        }
    }

    override fun <T : Enum<T>> putEnum(keyName: String, value: T) {
        val data = readAllData()
        val updatedValues = data.values.toMutableMap()
        updatedValues[keyName] = value.name
        writeAllData(StorageData(updatedValues))
        SecureStorageEvents.emitEnumUpdate(keyName, value)
    }

    override fun <T : Enum<T>> getEnum(keyName: String, enumClass: KClass<T>): T? {
        val name = readAllData().values[keyName]
        return name?.let {
            runCatching { java.lang.Enum.valueOf(enumClass.java, it) }.getOrNull()
        }
    }

    override fun <T : Enum<T>> observeEnum(key: String, enumClass: KClass<T>): Flow<T?> = flow {
        emit(getEnum(key, enumClass))
        SecureStorageEvents.enumUpdates.collect { (k, v) ->
            if (k == key && enumClass.isInstance(v)) emit(enumClass.cast(v))
        }
    }

    override fun observeContains(key: String): Flow<Boolean> = flow {
        // Emit initial state
        emit(readAllData().values.containsKey(key))

        // Any time a put occurs → contains = true
        SecureStorageEvents.booleanUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
        SecureStorageEvents.stringUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
        SecureStorageEvents.enumUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
    }


    override fun remove(keyName: String) {
        val data = readAllData()
        val updatedValues = data.values.toMutableMap()
        updatedValues.remove(keyName)
        writeAllData(StorageData(updatedValues))
    }
}