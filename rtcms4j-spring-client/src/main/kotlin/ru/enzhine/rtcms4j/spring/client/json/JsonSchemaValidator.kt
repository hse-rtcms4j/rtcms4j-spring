package ru.enzhine.rtcms4j.spring.client.json

import ru.enzhine.rtcms4j.spring.client.json.exception.JsonValidationException

interface JsonSchemaValidator {
    @Throws(JsonValidationException::class)
    fun validateSchema(jsonSchema: String)

    @Throws(JsonValidationException::class)
    fun validateValuesBySchema(
        jsonValues: String,
        jsonSchema: String,
    )
}
