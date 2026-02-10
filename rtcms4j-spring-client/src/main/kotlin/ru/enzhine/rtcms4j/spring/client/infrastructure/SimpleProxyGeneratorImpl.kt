package ru.enzhine.rtcms4j.spring.client.infrastructure

import org.slf4j.LoggerFactory
import org.springframework.aop.TargetSource
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component

@Role(ROLE_INFRASTRUCTURE)
@Component
class SimpleProxyGeneratorImpl : SimpleProxyGenerator {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun wrapObject(
        target: Any,
        targetSource: TargetSource,
    ): Any {
        val targetClass = target.javaClass

        logger.debug("Initializing {} proxy factory...", targetClass)
        val proxyFactory =
            ProxyFactory(target).apply {
                this.isProxyTargetClass = true
                this.targetSource = targetSource
            }
        logger.debug("Initialized {} proxy factory. Creating proxy...", targetClass)

        val proxy = proxyFactory.proxy
        logger.debug("Created {} proxy.", targetClass)

        return proxy
    }
}
