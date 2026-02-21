package ru.enzhine.rtcms4j.spring.client.service.dto

import ru.enzhine.rtcms4j.spring.client.discovery.registry.dto.LocalConfigurationEntry

data class RemoteConfigurationEntry(
    val localEntry: LocalConfigurationEntry,
    val configurationId: Long,
    var currentVersion: String,
) {
    fun describe() =
        StringBuilder()
            .apply {
                append(
                    "Remote-configuration(",
                    "name=",
                    localEntry.configurationName,
                    ", ",
                    "remoteId=",
                    configurationId,
                    ", ",
                    "version=",
                    currentVersion,
                    ")",
                )
            }.toString()
}
