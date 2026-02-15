package ru.enzhine.rtcms4j.spring.client.version.props

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Role
import ru.enzhine.rtcms4j.spring.client.version.SemanticVersionResolveStrategy.Companion.VERSION_RESOLVE_STRATEGY_SEMVER_NAME

@Role(ROLE_INFRASTRUCTURE)
@ConfigurationProperties(prefix = "spring.rtcms4j.version.$VERSION_RESOLVE_STRATEGY_SEMVER_NAME")
data class SemanticVersionResolveProperties(
    val applyDifferentMajor: Boolean = false,
    val applyDifferentMinor: Boolean = true,
    val applyDifferentFix: Boolean = true,
    val semVerPattern: String = "^(?<major>[0-9]+)\\.(?<minor>[0-9]+)\\.(?<fix>[0-9]+)(?<extra>.*)$",
)
