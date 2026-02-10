package ru.enzhine.rtcms4j.spring.client.json

interface JsonSchemaGenerator {
    fun generateSchema(clazz: Class<*>): String
}
