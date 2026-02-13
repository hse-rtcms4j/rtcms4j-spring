package ru.enzhine.rtcms4j.spring.client.service.dto.mutator

interface ConfigurationMutator {
    fun getTarget(): Any

    fun updateValues(jsonValues: String)

    fun getJsonValuesWithVersion(version: String): String

    fun getJsonSchema(): String
}
