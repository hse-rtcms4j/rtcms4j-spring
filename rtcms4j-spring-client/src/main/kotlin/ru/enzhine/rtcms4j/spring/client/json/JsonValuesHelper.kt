package ru.enzhine.rtcms4j.spring.client.json

interface JsonValuesHelper {
    fun generateValues(
        obj: Any,
        version: String,
    ): String

    fun excludeVersionFromValuesString(values: String): String
}
