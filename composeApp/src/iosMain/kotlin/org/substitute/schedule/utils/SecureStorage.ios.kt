package org.substitute.schedule.utils

import platform.Security.*
import platform.Foundation.*
import kotlinx.cinterop.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.substitute.schedule.utils.SecureStorageEvents
import kotlin.reflect.KClass
import kotlin.reflect.cast

class IOSSecureStorage : SecureStorage {
    override fun putString(key: String, value: String) {
        val data = value.encodeToByteArray().toNSData()
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
        val addQuery = query + (kSecValueData to data)
        SecItemAdd(addQuery, null)

        // Emit the change globally
        SecureStorageEvents.emitStringUpdate(key, value)
    }

    override fun getString(key: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, resultPtr.ptr)
            if (status == errSecSuccess) {
                val data = resultPtr.value as NSData
                return data.toByteArray().decodeToString()
            }
        }
        return null
    }

    override fun putBoolean(key: String, value: Boolean) {
        val data = value.toString().encodeToByteArray().toNSData()
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
        val addQuery = query + (kSecValueData to data)
        SecItemAdd(addQuery, null)

        // Emit the change globally
        SecureStorageEvents.emitBooleanUpdate(key, value)
    }

    override fun getBoolean(key: String): Boolean {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, resultPtr.ptr)
            if (status == errSecSuccess) {
                val data = resultPtr.value as NSData
                return data.toByteArray().decodeToString().toBoolean()
            }
        }
        return false
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

    override fun <T : Enum<T>> putEnum(key: String, value: T) {
        val data = value.name.encodeToByteArray().toNSData()
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
        val addQuery = query + (kSecValueData to data)
        SecItemAdd(addQuery, null)

        SecureStorageEvents.emitEnumUpdate(key, value)
    }

    override fun <T : Enum<T>> getEnum(key: String, enumClass: KClass<T>): T? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, resultPtr.ptr)
            if (status == errSecSuccess) {
                val data = resultPtr.value as NSData
                val name = data.toByteArray().decodeToString()
                return runCatching { java.lang.Enum.valueOf(enumClass.java, name) }.getOrNull()
            }
        }
        return null
    }

    override fun <T : Enum<T>> observeEnum(key: String, enumClass: KClass<T>): Flow<T?> = flow {
        emit(getEnum(key, enumClass))
        SecureStorageEvents.enumUpdates.collect { (k, v) ->
            if (k == key && enumClass.isInstance(v)) emit(enumClass.cast(v))
        }
    }

    override fun observeContains(key: String): Flow<Boolean> = flow {
        // Initial state
        emit(
            getString(key) != null || getBoolean(key) != false || getEnum<Any>(
                key,
                Any::class
            ) != null
        )

        // Any update event means the key is present
        SecureStorageEvents.stringUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
        SecureStorageEvents.booleanUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
        SecureStorageEvents.enumUpdates.collect { (k, _) ->
            if (k == key) emit(true)
        }
    }

    override fun remove(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
    }
}