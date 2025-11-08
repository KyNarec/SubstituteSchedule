package org.substitute.schedule.utils

import platform.Security.*
import platform.Foundation.*
import kotlinx.cinterop.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.substitute.schedule.utils.SecureStorageEvents

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

    override fun remove(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
    }
}