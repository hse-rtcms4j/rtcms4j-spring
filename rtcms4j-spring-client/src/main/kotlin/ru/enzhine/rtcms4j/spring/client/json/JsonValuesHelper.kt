package ru.enzhine.rtcms4j.spring.client.json

interface JsonValuesHelper {
    fun generateValues(
        obj: Any,
        version: String,
    ): String

    fun extractVersionFromValuesString(values: String): Pair<String, String>
}
