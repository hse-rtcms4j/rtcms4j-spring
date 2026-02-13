package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.spring.client.service.dto.BackendState
import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

interface RemoteConfigurationManager {
    @Throws(
        BackendConfigurationException.NotFound::class,
        BackendConfigurationException.NoState::class,
    )
    fun fetchRemote(remoteConfigurationEntry: RemoteConfigurationEntry): BackendState

    fun tryUpdateValuesByRemote(
        remoteConfigurationEntry: RemoteConfigurationEntry,
        remoteVersion: String,
        jsonValues: String,
    )

    @Throws(
        BackendConfigurationException.NotFound::class,
        BackendConfigurationException.CommitFailed::class,
    )
    fun commitToRemote(remoteConfigurationEntry: RemoteConfigurationEntry)

    @Throws(
        BackendConfigurationException.CreateFailed::class,
    )
    fun createNewRemote(remoteConfigurationEntry: RemoteConfigurationEntry)
}
