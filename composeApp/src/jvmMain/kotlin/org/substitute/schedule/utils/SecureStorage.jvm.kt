package org.substitute.schedule.utils

import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class DesktopSecureStorage : SecureStorage {
    private val file = File(System.getProperty("user.home"), ".securestore")
    private val key = SecretKeySpec("your-32-byte-key-goes-here!".toByteArray(), "AES")

    override fun putString(keyName: String, value: String) {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(value.toByteArray())
        file.writeBytes(encrypted)
    }

    override fun getString(keyName: String): String? {
        if (!file.exists()) return null
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypted = cipher.doFinal(file.readBytes())
        return String(decrypted)
    }

    override fun remove(keyName: String) {
        if (file.exists()) file.delete()
    }
}
