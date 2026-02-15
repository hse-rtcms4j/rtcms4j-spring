package ru.enzhine.rtcms4j.spring.client.service.dto

import ru.enzhine.rtcms4j.spring.client.service.dto.mutator.ConfigurationMutator
import ru.enzhine.rtcms4j.spring.client.version.VersionResolveStrategy

data class RemoteConfigurationEntry(
    val beanName: String,
    val beanClass: Class<*>,
    val configName: String,
    var configId: Long?,
    var version: String,
    val versionResolveStrategy: VersionResolveStrategy,
    val configurationMutator: ConfigurationMutator,
) {
    fun describe(): String {
        val sb = StringBuilder()
        sb.append("RemoteConfiguration $beanName")
        if (configId != null) {
            sb.append(" with id=$configId")
        }
        sb.append(" with version=$version")
        return sb.toString()
    }
}
