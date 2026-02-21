package ru.enzhine.rtcms4j.spring.client.discovery.registry.dto

import ru.enzhine.rtcms4j.spring.client.discovery.mutator.ConfigurationMutator
import ru.enzhine.rtcms4j.spring.client.version.VersionResolveStrategy

data class LocalConfigurationEntry(
    val beanName: String,
    val beanClass: Class<*>,
    val configurationName: String,
    val configId: Long?,
    val initialVersion: String,
    val versionResolveStrategy: VersionResolveStrategy,
    val configurationMutator: ConfigurationMutator,
) {
    fun describe(): String {
        val sb = StringBuilder()
        sb.append(configurationName)
        if (configId != null) {
            sb.append(" with id=$configId")
        }
        sb.append(" and version=$initialVersion")
        return sb.toString()
    }
}
