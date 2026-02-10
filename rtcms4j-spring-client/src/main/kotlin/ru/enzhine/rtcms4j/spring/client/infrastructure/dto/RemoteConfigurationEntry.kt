package ru.enzhine.rtcms4j.spring.client.infrastructure.dto

import org.springframework.aop.target.HotSwappableTargetSource
import ru.enzhine.rtcms4j.spring.client.version.VersionResolveStrategy

data class RemoteConfigurationEntry(
    val beanName: String,
    val beanClass: Class<*>,
    val configName: String,
    val configId: Long?,
    var version: String,
    val versionResolveStrategy: VersionResolveStrategy,
    val mutableInstance: Any?,
    val mutableTargetSource: HotSwappableTargetSource?,
)
