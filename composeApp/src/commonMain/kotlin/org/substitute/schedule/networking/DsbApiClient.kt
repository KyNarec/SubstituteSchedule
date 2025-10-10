package org.substitute.schedule.networking

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.network.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.*
import okio.IOException
import org.substitute.schedule.networking.util.Gzip
import org.substitute.schedule.networking.util.NetworkError
import org.substitute.schedule.networking.util.Result
import org.substitute.schedule.networking.util.onSuccess
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class TimeTable(
    val uuid: Uuid,
    val groupName: String,
    val date: String,
    val title: String,
    val detail: String
)

class DsbApiClient(
    private val httpClient: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val args = mutableMapOf<String, Any?>()

    init {
        args["UserId"] = "XX"
        args["UserPw"] = "XX"
        args["Language"] = "de"

        args["Device"] = "Nexus 4"
        args["AppId"] = uuid4().toString()
        args["AppVersion"] = "2.5.9"
        args["OsVersion"] = "27 8.1.0"

        args["PushId"] = ""
        args["BundleId"] = "de.heinekingmedia.dsbmobile"
    }

    suspend fun pullData(): Result<Any?, NetworkError> {
        val response = try {
            httpClient.post {
                url("https://www.dsbmobile.de/JsonHandler.ashx/GetData")
                userAgent("Dalvik/2.1.0 (Linux; U; Android 8.1.0; Nexus 4 Build/OPM7.181205.001)")
                header(HttpHeaders.AcceptEncoding, "gzip, deflate")
                header(HttpHeaders.ContentType, "application/json;charset=utf-8")
                val packageArgs = packageArgs()
//                println("packagedArgs: $packageArgs")
                setBody(packageArgs)
            }
        } catch (e: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                Result.Success(decodeJson(response = response.body()))
            }
            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun getTimeTables(): List<TimeTable> {
        val timeTables = mutableListOf<TimeTable>()
        var successfulJsonResponse: JsonObject = JsonObject(emptyMap())

        pullData().onSuccess {
            if (it !is JsonObject) {
                throw IllegalStateException("Response is not a JsonObject")
            } else {
                successfulJsonResponse = it
            }
        }

        val contentObject = findJsonObjectByTitle(
            successfulJsonResponse["ResultMenuItems"]?.jsonArray ?: JsonArray(emptyList()),
            "Inhalte"
        ) ?: throw IllegalStateException("Field 'Inhalt' is null")

        val tableObject = findJsonObjectByTitle(
            contentObject["Childs"]?.jsonArray ?: JsonArray(emptyList()),
            "Pläne"
        ) ?: throw IllegalStateException("Field 'Pläne' is null")

        val rootChilds = tableObject["Root"]?.jsonObject?.get("Childs")?.jsonArray
            ?: JsonArray(emptyList())

        for (jElement in rootChilds) {
            if (jElement !is JsonObject) continue

            val uuid = uuidFrom(jElement["Id"]?.jsonPrimitive?.content ?: continue)
            val groupName = jElement["Title"]?.jsonPrimitive?.content ?: ""
            val date = jElement["Date"]?.jsonPrimitive?.content ?: ""

            val childs = jElement["Childs"]?.jsonArray ?: continue

            for (jElementChild in childs) {
                if (jElementChild !is JsonObject) continue

                val title = jElementChild["Title"]?.jsonPrimitive?.content ?: ""
                val detail = jElementChild["Detail"]?.jsonPrimitive?.content ?: ""

                timeTables.add(TimeTable(uuid, groupName, date, title, detail))
            }
        }
        return timeTables
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Throws(IOException::class)
    private fun packageArgs(): String {
        val date = getFormattedTime(Clock.System.now())
        args["Date"] = date
        args["LastUpdate"] = date

        val argsJson = buildJsonObject {
            args.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Number -> put(key, JsonPrimitive(value))
                    null -> put(key, JsonNull)
                    else -> put(key, value.toString())
                }
            }
        }

        val compressedData = Base64.encode(Gzip.compress(argsJson.toString()))

        val innerArgs = buildJsonObject {
            put("Data", compressedData)
            put("DataType", 1)
        }

        val outerArgs = buildJsonObject {
            put("req", innerArgs)
        }

        return outerArgs.toString()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeJson(response: String): JsonObject {
        val responseJson = json.parseToJsonElement(response).jsonObject
        val compressedData = responseJson["d"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("Field 'd' is null")

        val decodedBytes = Base64.decode(compressedData)
        val decompressed = Gzip.decompress(decodedBytes)
        val resultJson = json.parseToJsonElement(decompressed).jsonObject

        return resultJson
    }

    private fun getFormattedTime(instant: Instant): String {
        val dateTime = instant.toLocalDateTime(TimeZone.UTC)

        // Format: yyyy-MM-dd'T'HH:mm:ss.SSSZ
        val year = dateTime.year
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')
        val second = dateTime.second.toString().padStart(2, '0')
        val millis = (instant.nanosecondsOfSecond / 1_000_000).toString().padStart(3, '0')

        return "$year-$month-${day}T$hour:$minute:$second.${millis}+0000"
    }

    private fun findJsonObjectByTitle(sourceArray: JsonArray, title: String): JsonObject? {
        for (jElement in sourceArray) {
            if (jElement !is JsonObject) continue

            val objectTitle = jElement["Title"]?.jsonPrimitive?.content ?: continue
            if (objectTitle.equals(title, ignoreCase = true)) return jElement
        }
        return null
    }
}