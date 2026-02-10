package ru.enzhine.rtcms4j.spring.client.infrastructure

import org.springframework.beans.factory.config.BeanDefinition
import ru.enzhine.rtcms4j.spring.client.infrastructure.exception.RemoteConfigurationBeanValidationException

interface RemoteConfigurationBeanValidator {
    @Throws(RemoteConfigurationBeanValidationException::class)
    fun validateAndPrepareBeanDefinition(
        beanName: String,
        clazz: Class<*>,
        beanDefinition: BeanDefinition,
    )
}
