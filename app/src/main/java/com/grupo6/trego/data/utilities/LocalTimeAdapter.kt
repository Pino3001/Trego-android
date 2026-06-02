package com.grupo6.trego.data.utilities

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun serialize(
        src: LocalTime?,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalTime? {
        return try {
            val timeStr = json.asString
            if (timeStr.isNullOrEmpty()) {
                null
            } else {
                LocalTime.parse(timeStr, formatter)
            }
        } catch (e: Exception) {
            null
        }
    }
}
