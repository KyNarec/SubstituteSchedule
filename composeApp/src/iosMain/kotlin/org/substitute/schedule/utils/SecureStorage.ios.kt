package org.substitute.schedule.utils

import platform.Security.*
import platform.Foundation.*

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

    override fun remove(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
    }
}