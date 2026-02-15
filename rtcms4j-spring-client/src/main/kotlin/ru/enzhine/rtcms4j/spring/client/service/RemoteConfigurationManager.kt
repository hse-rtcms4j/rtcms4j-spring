package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

interface RemoteConfigurationManager {
    @Throws(
        BackendConfigurationException.FetchFailed::class,
        BackendConfigurationException.CreateFailed::class,
    )
    fun tryUpdate(remoteConfigurationEntries: List<RemoteConfigurationEntry>)

    fun tryUpdateSingleDirectly(
        remoteConfigurationEntry: RemoteConfigurationEntry,
        content: String,
    )
}
