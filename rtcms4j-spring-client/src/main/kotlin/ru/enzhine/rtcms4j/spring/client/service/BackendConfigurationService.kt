package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDetailedDto
import ru.enzhine.rtcms4j.spring.client.discovery.mutator.ConfigurationMutator
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendState
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

interface BackendConfigurationService {
    @Throws(
        exceptionClasses = [
            BackendConfigurationException.FetchFailed::class,
        ],
    )
    fun getBackendConfigurations(): List<BackendConfigurationEntry>

    @Throws(
        exceptionClasses = [
            BackendConfigurationException.CreationFailed::class,
        ],
    )
    fun createNewRemote(configurationName: String): ConfigurationDetailedDto

    @Throws(
        BackendConfigurationException.FetchFailed::class,
    )
    fun fetchRemote(configurationId: Long): BackendState?

    @Throws(
        BackendConfigurationException.CommitFailed::class,
    )
    fun commitToRemote(
        configurationId: Long,
        configurationMutator: ConfigurationMutator,
        version: String,
    ): ConfigurationCommitDetailedDto
}
