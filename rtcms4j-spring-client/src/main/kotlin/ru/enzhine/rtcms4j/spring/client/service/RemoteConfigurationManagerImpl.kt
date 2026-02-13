package ru.enzhine.rtcms4j.spring.client.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDtoCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.SourceType
import ru.enzhine.rtcms4j.notify.api.NotifyApi
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.mapper.toService
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendState
import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

@Service
class RemoteConfigurationManagerImpl(
    private val backendConfigurationProvider: BackendConfigurationProvider,
    private val rtcms4jProperties: Rtcms4jProperties,
    private val coreApi: CoreApi,
    private val notifyApi: NotifyApi,
) : RemoteConfigurationManager {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun fetchRemote(remoteConfigurationEntry: RemoteConfigurationEntry): BackendState {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val backendConfig = findBackendConfigurationOrThrow(remoteConfigurationEntry)
        val cid = backendConfig.configId
        if (remoteConfigurationEntry.configId == null) {
            remoteConfigurationEntry.configId = cid
        }

        if (backendConfig.version == null) {
            throw BackendConfigurationException.NoState(
                message = "RemoteConfigurationEntry has no state.",
                parent = null,
            )
        }

        val backendConfiguration =
            try {
                coreApi.getConfiguration(nid, aid, cid)
            } catch (ex: RestClientResponseException) {
                throw BackendConfigurationException.FetchFailed(
                    message = "Backend configuration with id '$cid' not found.",
                    parent = ex,
                )
            }

        return backendConfiguration.toService()
            ?: throw BackendConfigurationException.NoState(
                message = "RemoteConfigurationEntry has no state.",
                parent = null,
            )
    }

    override fun tryUpdateValuesByRemote(
        remoteConfigurationEntry: RemoteConfigurationEntry,
        remoteVersion: String,
        jsonValues: String,
    ) {
        val versionResolver = remoteConfigurationEntry.versionResolveStrategy
        val previousVersion = remoteConfigurationEntry.version
        if (
            previousVersion == remoteVersion ||
            !versionResolver.shouldApplyNewVersion(previousVersion, remoteVersion)
        ) {
            return
        }

        remoteConfigurationEntry.configurationMutator.updateValues(jsonValues)
    }

    override fun commitToRemote(remoteConfigurationEntry: RemoteConfigurationEntry) {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val backendConfig = findBackendConfigurationOrThrow(remoteConfigurationEntry)
        val cid = backendConfig.configId
        if (remoteConfigurationEntry.configId == null) {
            remoteConfigurationEntry.configId = cid
        }

        val commitVersion = remoteConfigurationEntry.version
        val request =
            ConfigurationCommitRequest().apply {
                jsonSchema = remoteConfigurationEntry.configurationMutator.getJsonSchema()
                jsonValues = remoteConfigurationEntry.configurationMutator.getJsonValuesWithVersion(commitVersion)
            }

        try {
            val commit = coreApi.commitConfiguration(nid, aid, backendConfig.configId, request)
            backendConfig.version = commit.commitVersion
        } catch (ex: RestClientResponseException) {
            throw BackendConfigurationException.CommitFailed(
                message = "Backend configuration with id '$cid' commit failed.",
                parent = ex,
            )
        }
    }

    override fun createNewRemote(remoteConfigurationEntry: RemoteConfigurationEntry) {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val request =
            ConfigurationDtoCreateRequest().apply {
                name = remoteConfigurationEntry.configName
                schemaSourceType = SourceType.SERVICE
            }

        try {
            val remote = coreApi.createConfiguration(nid, aid, request)

            val cid = remote.id
            if (remoteConfigurationEntry.configId == null) {
                remoteConfigurationEntry.configId = cid
            }

            backendConfigurationProvider.evictBackendConfigurations()
        } catch (ex: RestClientResponseException) {
            throw BackendConfigurationException.CreateFailed(
                message = "Remote configuration '${remoteConfigurationEntry.configName}' creation failed.",
                parent = ex,
            )
        }
    }

    private fun findBackendConfigurationOrThrow(remoteConfigurationEntry: RemoteConfigurationEntry): BackendConfigurationEntry {
        val backendEntries = backendConfigurationProvider.getBackendConfigurations()

        val configId = remoteConfigurationEntry.configId
        val backendConfig =
            if (configId != null) {
                backendEntries.find { it.configId == configId }
                    ?: throw BackendConfigurationException.NotFound(
                        message = "Backend configuration with id '$configId' not found.",
                        parent = null,
                    )
            } else {
                val backendName = remoteConfigurationEntry.configName
                backendEntries.find { it.configName == backendName }
                    ?: throw BackendConfigurationException.NotFound(
                        message = "Backend configuration named '$backendName' not found.",
                        parent = null,
                    )
            }

        return backendConfig
    }
}
