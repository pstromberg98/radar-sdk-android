package io.radar.sdk.model

import io.radar.sdk.Radar
import org.json.JSONObject
import java.util.*

/**
 * Represents a log line.
 */
internal data class RadarLog(
    val level: Radar.RadarLogLevel,
    val message: String,
    val type: Radar.RadarLogType?,
    val createdAt: Date = Date()
) : Comparable<RadarLog> {

    companion object {
        private const val CREATED_AT = "createdAt"
        private const val LEVEL = "level"
        private const val TYPE = "type"
        private const val MESSAGE = "message"

        @JvmStatic
        fun fromJson(json: JSONObject): RadarLog {
            val levelString = json.optString(LEVEL)
            val typeString = json.optString(TYPE)
            return RadarLog(
                level = if (levelString.isNotBlank()) Radar.RadarLogLevel.valueOf(levelString) else Radar.RadarLogLevel.INFO,
                type = if (typeString.isNotBlank() && typeString!="NONE") Radar.RadarLogType.valueOf(typeString) else null,
                message = json.optString(MESSAGE),
                createdAt = Date(json.optLong(CREATED_AT))
            )
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            putOpt(CREATED_AT, createdAt.time)
            putOpt(LEVEL, level.name)
            putOpt(TYPE, type?.name)
            putOpt(MESSAGE, message)
        }
    }

    override fun compareTo(other: RadarLog): Int {
        return createdAt.compareTo(other.createdAt)
    }
}
