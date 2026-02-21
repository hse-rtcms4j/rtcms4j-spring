package ru.enzhine.rtcms4j.spring.client.discovery

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.aop.target.HotSwappableTargetSource
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.EmbeddedValueResolverAware
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.util.StringValueResolver
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration
import ru.enzhine.rtcms4j.spring.client.discovery.mutator.ImmutableConfigurationMutator
import ru.enzhine.rtcms4j.spring.client.discovery.mutator.MutableConfigurationMutator
import ru.enzhine.rtcms4j.spring.client.discovery.proxy.SimpleProxyGenerator
import ru.enzhine.rtcms4j.spring.client.discovery.registry.LocalConfigurationRegistry
import ru.enzhine.rtcms4j.spring.client.discovery.registry.dto.LocalConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.version.VersionResolveStrategy

@Component
class RemoteConfigurationBeansRegistrar(
    private val localConfigurationRegistry: LocalConfigurationRegistry,
    private val simpleProxyGenerator: SimpleProxyGenerator,
    private val versionResolveStrategies: Map<String, VersionResolveStrategy>,
) : BeanPostProcessor,
    ApplicationContextAware,
    EmbeddedValueResolverAware {
    private lateinit var stringValueResolver: StringValueResolver
    private lateinit var beanFactory: ConfigurableListableBeanFactory

    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

    override fun setEmbeddedValueResolver(resolver: StringValueResolver) {
        stringValueResolver = resolver
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        beanFactory = applicationContext.autowireCapableBeanFactory as ConfigurableListableBeanFactory
    }

    override fun postProcessBeforeInitialization(
        bean: Any,
        beanName: String,
    ): Any? {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            return super.postProcessBeforeInitialization(bean, beanName)
        }
        val definition = beanFactory.getBeanDefinition(beanName)
        val beanClassName =
            definition.beanClassName
                ?: return super.postProcessBeforeInitialization(bean, beanName)

        val clazz = Class.forName(beanClassName)
        val annotation: MergedAnnotation<RemoteConfiguration> =
            MergedAnnotations
                .from(clazz, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(RemoteConfiguration::class.java)

        if (!annotation.isPresent) {
            return super.postProcessBeforeInitialization(bean, beanName)
        }

        val configurationName = getConfigurationName(clazz, annotation)
        val configurationId = getConfigurationId(annotation)
        val configurationVersion = getConfigurationVersion(annotation)
        val versionResolveStrategy = getConfigurationVersionResolveStrategy(annotation)

        var returnBean = bean

        val requiresProxy = definition.getAttribute("requiresProxy") as Boolean
        val mutator =
            if (requiresProxy) {
                val swappableTargetSource = HotSwappableTargetSource(bean)
                val proxy = simpleProxyGenerator.wrapObject(bean, swappableTargetSource)
                returnBean = proxy

                ImmutableConfigurationMutator(objectMapper, swappableTargetSource)
            } else {
                MutableConfigurationMutator(objectMapper, bean, clazz)
            }

        localConfigurationRegistry.register(
            LocalConfigurationEntry(
                beanName = beanName,
                beanClass = clazz,
                configurationName = configurationName,
                configId = configurationId,
                initialVersion = configurationVersion,
                versionResolveStrategy = versionResolveStrategy,
                configurationMutator = mutator,
            ),
        )

        return returnBean
    }

    private fun getConfigurationName(
        clazz: Class<*>,
        annotation: MergedAnnotation<RemoteConfiguration>,
    ): String {
        val aliasName =
            annotation
                .getString("aliasName")
                .takeIf { StringUtils.hasText(it) }
                ?.let { resolveSpelString(it) }

        val className = clazz.simpleName

        return aliasName ?: className
    }

    private fun getConfigurationId(annotation: MergedAnnotation<RemoteConfiguration>): Long? =
        annotation
            .getLong("remoteId")
            .takeIf { it >= 0L }

    private fun getConfigurationVersion(annotation: MergedAnnotation<RemoteConfiguration>): String =
        annotation
            .getString("version")
            .takeIf { StringUtils.hasText(it) }
            ?.let { resolveSpelString(it) }
            ?: throw RuntimeException("Configuration version not present.")

    private fun getConfigurationVersionResolveStrategy(annotation: MergedAnnotation<RemoteConfiguration>): VersionResolveStrategy {
        val versionResolveStrategy =
            annotation
                .getString("versionResolveStrategy")
                .takeIf { StringUtils.hasText(it) }
                ?: throw RuntimeException("Configuration version resolve strategy not present.")

        val strategy =
            versionResolveStrategies[versionResolveStrategy]
                ?: throw RuntimeException("Configuration version resolve strategy named '$versionResolveStrategy' not present.")

        return strategy
    }

    private fun resolveSpelString(expression: String) =
        try {
            stringValueResolver.resolveStringValue(expression)
        } catch (_: IllegalArgumentException) {
            expression
        }
}
