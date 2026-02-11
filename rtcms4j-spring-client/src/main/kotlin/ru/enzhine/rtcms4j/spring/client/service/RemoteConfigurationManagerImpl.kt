package ru.enzhine.rtcms4j.spring.client.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.notify.api.NotifyApi
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.mapper.toService
import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationNotMatchedException

@Service
class RemoteConfigurationManagerImpl(
    private val backendConfigurationProvider: BackendConfigurationProvider,
    private val rtcms4jProperties: Rtcms4jProperties,
    private val coreApi: CoreApi,
    private val notifyApi: NotifyApi,
    private val objectMapper: ObjectMapper,
) : RemoteConfigurationManager {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun fetchRemoteAndUpdate(remoteConfigurationEntry: RemoteConfigurationEntry) {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val backendEntries = backendConfigurationProvider.getBackendConfigurations()

        val configId = remoteConfigurationEntry.configId
        val backendConfig =
            if (configId != null) {
                backendEntries.find { it.configId == configId }
                    ?: throw BackendConfigurationNotMatchedException(
                        message = "Backend configuration with id '$configId' not found.",
                        parent = null,
                    )
            } else {
                val backendName = remoteConfigurationEntry.configName
                backendEntries.find { it.configName == backendName }
                    ?: throw BackendConfigurationNotMatchedException(
                        message = "Backend configuration named '$backendName' not found.",
                        parent = null,
                    )
            }

        // TODO: remote state absence case
        val remoteState = coreApi.getConfiguration(nid, aid, backendConfig.configId).toService()

        val versionResolver = remoteConfigurationEntry.versionResolveStrategy
        val previousVersion = remoteConfigurationEntry.version
        val remoteVersion = remoteState.version
        if (previousVersion == remoteVersion || !versionResolver.shouldApplyNewVersion(previousVersion, remoteVersion)) {
            return
        }

        updateValues(remoteConfigurationEntry, remoteState.jsonValues)
    }

    private fun updateValues(
        remoteConfigurationEntry: RemoteConfigurationEntry,
        jsonValues: String,
    ) {
        val valueType = remoteConfigurationEntry.beanClass

        val objectReader = remoteConfigurationEntry.mutableObjectReader
        if (objectReader != null) {
            objectReader.readValue(jsonValues, valueType)
            return
        }

        val targetSource = remoteConfigurationEntry.mutableTargetSource
        if (targetSource != null) {
            val newTarget = objectMapper.readValue(jsonValues, valueType)
            targetSource.swap(newTarget)
            return
        }

        throw RuntimeException(
            "RemoteConfigurationEntry has no providers for instance update. " +
                "This is critical situation and must not be reached.",
        )
    }
}
