package ru.enzhine.rtcms4j.spring.client.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.config.props.FeaturesProperties
import ru.enzhine.rtcms4j.spring.client.discovery.registry.LocalConfigurationRegistry
import ru.enzhine.rtcms4j.spring.client.discovery.registry.dto.LocalConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

@Component
class RemoteConfigurationRegistryImpl(
    localConfigurationRegistry: LocalConfigurationRegistry,
    backendConfigurationService: BackendConfigurationService,
    featuresProperties: FeaturesProperties,
) : RemoteConfigurationRegistry {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val beanEntries = mutableSetOf<RemoteConfigurationEntry>()

    init {
        val localConfigurations = localConfigurationRegistry.entries()
        val backendConfigurations = backendConfigurationService.getBackendConfigurations()

        for (local in localConfigurations) {
            try {
                val backendFound =
                    findBackendConfigurationOrThrow(local, backendConfigurations)

                beanEntries.add(
                    RemoteConfigurationEntry(
                        localEntry = local,
                        configurationId = backendFound.configurationId,
                        currentVersion = local.initialVersion,
                    ),
                )
            } catch (ex: BackendConfigurationException.NotMatched) {
                if (featuresProperties.ignoreConfigurationOnMatchFailure) {
                    logger.info("Ignoring $local match: ${ex.message}")
                } else {
                    throw ex
                }
            } catch (_: BackendConfigurationException.NotFound) {
                try {
                    val backendCreated =
                        backendConfigurationService.createNewRemote(local.configurationName)

                    beanEntries.add(
                        RemoteConfigurationEntry(
                            localEntry = local,
                            configurationId = backendCreated.id,
                            currentVersion = local.initialVersion,
                        ),
                    )
                } catch (ex: BackendConfigurationException.CreationFailed) {
                    if (featuresProperties.ignoreConfigurationOnCreationFailure) {
                        logger.info("Ignoring $local remote creation: ${ex.message}")
                    } else {
                        throw ex
                    }
                }
            }
        }
    }

    @Throws(
        exceptionClasses = [
            BackendConfigurationException.NotMatched::class,
            BackendConfigurationException.NotFound::class,
        ],
    )
    private fun findBackendConfigurationOrThrow(
        localConfigurationEntry: LocalConfigurationEntry,
        backends: List<BackendConfigurationEntry>,
    ): BackendConfigurationEntry {
        val configId = localConfigurationEntry.configId
        val backendConfig =
            if (configId != null) {
                backends.find { it.configurationId == configId }
                    ?: throw BackendConfigurationException.NotMatched(
                        "Backend-configuration not matched: $localConfigurationEntry" +
                            " does not match with remotes by id '$configId'.",
                    )
            } else {
                val backendName = localConfigurationEntry.configurationName
                backends.find { it.configurationName == backendName }
                    ?: throw BackendConfigurationException.NotFound(
                        "Backend-configuration not found: $localConfigurationEntry" +
                            " does not match with remotes by name '$backendName'.",
                    )
            }

        return backendConfig
    }

    override fun entries(): List<RemoteConfigurationEntry> = beanEntries.toList()
}
