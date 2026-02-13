package ru.enzhine.rtcms4j.spring.client.version.props

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Role

@Role(ROLE_INFRASTRUCTURE)
@ConfigurationProperties(prefix = "spring.rtcms4j.strategies.sem-ver")
data class SemanticVersionResolveProperties(
    val applyDifferentMajor: Boolean = false,
    val applyDifferentMinor: Boolean = true,
    val applyDifferentFix: Boolean = true,
    val semVerPattern: String = "^(?<major>[0-9]+)\\.(?<minor>[0-9]+)\\.(?<fix>[0-9]+)(?<extra>.*)$",
)
