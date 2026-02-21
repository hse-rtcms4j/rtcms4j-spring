package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

interface RemoteConfigurationManager {
    @Throws(
        exceptionClasses = [
            BackendConfigurationException.FetchFailed::class,
            BackendConfigurationException.CommitFailed::class,
        ],
    )
    fun tryUpdateMultipleAuto(remoteConfigurations: List<RemoteConfigurationEntry>): Int

    fun tryUpdateSingleDirectly(
        remoteConfiguration: RemoteConfigurationEntry,
        jsonValues: String,
    ): Boolean
}
