import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.substitute.schedule.utils.SecureStorage

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
    }

    override fun getString(keyName: String): String? {
        return readAllData().values[keyName]
    }

    override fun putBoolean(keyName: String, value: Boolean) {
        val data = readAllData()
        val updatedValues = data.values.toMutableMap()
        updatedValues[keyName] = value.toString()
        writeAllData(StorageData(updatedValues))
    }

    override fun getBoolean(keyName: String): Boolean {
        return readAllData().values[keyName]?.toBoolean() ?: false
    }

    override fun remove(keyName: String) {
        val data = readAllData()
        val updatedValues = data.values.toMutableMap()
        updatedValues.remove(keyName)
        writeAllData(StorageData(updatedValues))
    }
}