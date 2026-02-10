package ru.enzhine.rtcms4j.spring.client.processor

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration
import ru.enzhine.rtcms4j.spring.client.infrastructure.RemoteConfigurationBeanValidator
import ru.enzhine.rtcms4j.spring.client.infrastructure.RemoteConfigurationBeanValidatorImpl

@Component
class RemoteConfigurationBeansObserver : BeanDefinitionRegistryPostProcessor {
    private val remoteConfigurationBeanValidator: RemoteConfigurationBeanValidator =
        RemoteConfigurationBeanValidatorImpl()

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        for (beanName in registry.beanDefinitionNames) {
            val beanDefinition = registry.getBeanDefinition(beanName)
            val beanClassName =
                beanDefinition.beanClassName
                    ?: continue

            val clazz = Class.forName(beanClassName)
            val annotation: MergedAnnotation<RemoteConfiguration> =
                MergedAnnotations
                    .from(clazz, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                    .get(RemoteConfiguration::class.java)

            if (!annotation.isPresent) {
                continue
            }

            remoteConfigurationBeanValidator.validateAndPrepareBeanDefinition(beanName, clazz, beanDefinition)
        }
    }
}
