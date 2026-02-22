package ru.enzhine.rtcms4j.spring.client.json

import com.networknt.schema.InputFormat
import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SchemaRegistryConfig
import com.networknt.schema.SpecificationVersion
import com.networknt.schema.regex.JoniRegularExpressionFactory
import ru.enzhine.rtcms4j.spring.client.json.exception.JsonValidationException

class JsonSchemaValidatorImpl : JsonSchemaValidator {
    private val metaSchemaClasspath = "json/schema/meta-schema.json"

    private val schemaRegistryConfig =
        SchemaRegistryConfig
            .builder()
            .regularExpressionFactory(JoniRegularExpressionFactory.getInstance())
            .build()

    private val schemaRegistry =
        SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12) {
            it.schemaRegistryConfig(schemaRegistryConfig)
        }

    private val metaSchema =
        ClassLoader.getSystemResourceAsStream(metaSchemaClasspath).use {
            schemaRegistry.getSchema(it)
        }

    override fun validateSchema(jsonSchema: String) = validateJsonBySchema(jsonSchema, metaSchema)

    override fun validateValuesBySchema(
        jsonValues: String,
        jsonSchema: String,
    ) {
        val schema = schemaRegistry.getSchema(jsonSchema)
        validateJsonBySchema(jsonValues, schema)
    }

    private fun validateJsonBySchema(
        jsonValues: String,
        schema: Schema,
    ) {
        val errors = schema.validate(jsonValues, InputFormat.JSON)
        if (errors.isNotEmpty()) {
            throw JsonValidationException(errors.joinToString(", "))
        }
    }
}
