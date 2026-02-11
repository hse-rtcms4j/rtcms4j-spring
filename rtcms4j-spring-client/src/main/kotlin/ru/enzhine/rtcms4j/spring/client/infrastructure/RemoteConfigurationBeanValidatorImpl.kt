package ru.enzhine.rtcms4j.spring.client.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition
import ru.enzhine.rtcms4j.spring.client.infrastructure.exception.RemoteConfigurationBeanValidationException
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGenerator
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGeneratorImpl
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaValidator
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaValidatorImpl
import ru.enzhine.rtcms4j.spring.client.json.exception.JsonValidationException
import java.lang.reflect.Modifier

class RemoteConfigurationBeanValidatorImpl : RemoteConfigurationBeanValidator {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private val reservedProperties = arrayOf("version")

    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
        }

    private val jsonSchemaGenerator: JsonSchemaGenerator =
        JsonSchemaGeneratorImpl(objectMapper)

    private val jsonSchemaValidator: JsonSchemaValidator =
        JsonSchemaValidatorImpl()

    override fun validateAndPrepareBeanDefinition(
        beanName: String,
        clazz: Class<*>,
        beanDefinition: BeanDefinition,
    ) {
        validateClassIsJsonCompatible(beanName, clazz)

        validateAndPrepareClassIsJacksonCompatible(beanName, clazz, beanDefinition)
    }

    private fun validateClassIsJsonCompatible(
        beanName: String,
        beanClass: Class<*>,
    ) = try {
        val jsonSchema = jsonSchemaGenerator.generateSchema(beanClass)
        jsonSchemaValidator.validateSchema(jsonSchema)
    } catch (ex: JsonValidationException) {
        throw RemoteConfigurationBeanValidationException(
            message = "RemoteConfiguration bean $beanName $beanClass schema is not supported.",
            parent = ex,
        )
    }

    private fun validateAndPrepareClassIsJacksonCompatible(
        beanName: String,
        beanClass: Class<*>,
        beanDefinition: BeanDefinition,
    ) {
        val javaType = objectMapper.constructType(beanClass)
        val basicBeanDescription = objectMapper.serializationConfig.introspect(javaType)

        val availableBeanProperties = basicBeanDescription.findProperties()
        if (availableBeanProperties.isEmpty()) {
            throw RemoteConfigurationBeanValidationException(
                message = "RemoteConfiguration bean $beanName $beanClass has no serializable properties.",
                parent = null,
            )
        }

        for (reservedKeyword in reservedProperties) {
            if (availableBeanProperties.any { it.name == reservedKeyword }) {
                throw RemoteConfigurationBeanValidationException(
                    message =
                        "RemoteConfiguration bean $beanName $beanClass has reserved " +
                            "'$reservedKeyword' property that must not be used.",
                    parent = null,
                )
            }
        }
        val immutableProperties = availableBeanProperties.filter { !it.hasSetter() }
        val requiresProxy = immutableProperties.isNotEmpty()
        if (requiresProxy) {
            if (Modifier.isFinal(beanClass.modifiers)) {
                throw RemoteConfigurationBeanValidationException(
                    message =
                        "RemoteConfiguration bean $beanName $beanClass requires proxy " +
                            "(due to immutable properties: ${immutableProperties.joinToString(", ") { it.name }}" +
                            "), but class is final.",
                    parent = null,
                )
            }

            for (immutableProperty in immutableProperties) {
                if (Modifier.isFinal(immutableProperty.getter.modifiers)) {
                    throw RemoteConfigurationBeanValidationException(
                        message =
                            "RemoteConfiguration bean $beanName $beanClass requires proxy " +
                                "(due to immutable properties: ${immutableProperties.joinToString(", ") { it.name }}" +
                                "), but property ${immutableProperty.name} is final.",
                        parent = null,
                    )
                }
            }
        }

        beanDefinition.setAttribute("requiresProxy", requiresProxy)
    }
}
