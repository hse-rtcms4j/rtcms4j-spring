package ru.enzhine.rtcms4j.spring.client.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.spring.client.config.props.FeaturesProperties
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelper
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelperImpl
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException
import ru.enzhine.rtcms4j.spring.client.version.exception.RemoteConfigurationVersionException

@Service
class RemoteConfigurationManagerImpl(
    private val backendConfigurationService: BackendConfigurationService,
    private val featuresProperties: FeaturesProperties,
    private val feedbackService: FeedbackService,
    objectMapper: ObjectMapper,
) : RemoteConfigurationManager {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private val jsonValuesHelper: JsonValuesHelper =
        JsonValuesHelperImpl(objectMapper)

    override fun tryUpdateMultipleAuto(remoteConfigurations: List<RemoteConfigurationEntry>): Int {
        val backendConfigurations = backendConfigurationService.getBackendConfigurations()
        var updatedCount = 0

        for (remoteConfig in remoteConfigurations) {
            val backendConfig =
                backendConfigurations.find { it.configurationId == remoteConfig.configurationId }
                    ?: if (featuresProperties.skipConfigurationOnRemoteLostFailure) {
                        logger.warn("{} not found on last fetch.", remoteConfig.describe())
                        continue
                    } else {
                        throw BackendConfigurationException.NotFound(
                            "${remoteConfig.describe()} not found on last fetch.",
                        )
                    }

            try {
                if (updateConfiguration(remoteConfig, backendConfig)) {
                    updatedCount++
                }
            } catch (ex: RemoteConfigurationVersionException) {
                if (featuresProperties.skipConfigurationOnVersionFailure) {
                    logger.warn("${remoteConfig.describe()} version resolve failed during update.", ex)
                } else {
                    throw ex
                }
            } catch (ex: BackendConfigurationException.CommitFailed) {
                if (featuresProperties.skipConfigurationOnCommitFailure) {
                    logger.warn("${remoteConfig.describe()} new version commit failed during update.", ex)
                } else {
                    throw ex
                }
            } catch (ex: BackendConfigurationException.FetchFailed) {
                if (featuresProperties.skipConfigurationOnFetchFailure) {
                    logger.warn("${remoteConfig.describe()} remote version fetch failed during update.", ex)
                } else {
                    throw ex
                }
            }
        }

        return updatedCount
    }

    private fun updateConfiguration(
        remoteConfig: RemoteConfigurationEntry,
        backendConfig: BackendConfigurationEntry,
    ): Boolean {
        val configurationId = remoteConfig.configurationId
        val configurationMutator = remoteConfig.localEntry.configurationMutator

        if (shouldPostNewVersion(remoteConfig, backendConfig)) {
            backendConfigurationService.commitToRemote(
                configurationId,
                configurationMutator,
                remoteConfig.currentVersion,
            )
            logger.info("Commited new ${remoteConfig.describe()}.")
            feedbackService.postFeedbackOnConfiguration(configurationId, remoteConfig.currentVersion)

            return true
        }

        val nextVersion =
            backendConfig.version
                ?: run {
                    logger.warn(
                        "Remote-configuration with id='$configurationId' " +
                            "ignored: was not commited and has no state.",
                    )
                    return false
                }
        if (shouldApplyNewVersion(remoteConfig, nextVersion)) {
            val remoteState =
                backendConfigurationService.fetchRemote(configurationId)
                    ?: throw BackendConfigurationException.FetchFailed(
                        "Remote-configuration with id '$configurationId' " +
                            "is stateful, but empty state fetched.",
                    )

            configurationMutator.updateValues(remoteState.jsonValues)
            remoteConfig.currentVersion = remoteState.version
            feedbackService.postFeedbackOnConfiguration(configurationId, remoteConfig.currentVersion)
            logger.info("Updated ${remoteConfig.describe()}.")

            return true
        }

        logger.info("${remoteConfig.describe()} ignored version: $nextVersion")
        return false
    }

    private fun shouldPostNewVersion(
        remoteConfig: RemoteConfigurationEntry,
        backendConfig: BackendConfigurationEntry,
    ): Boolean {
        val remoteVersion = backendConfig.version
        val versionResolver = remoteConfig.localEntry.versionResolveStrategy
        val currentVersion = remoteConfig.currentVersion

        return remoteVersion != currentVersion &&
            versionResolver.shouldPostNewVersion(remoteVersion, currentVersion)
    }

    private fun shouldApplyNewVersion(
        remoteConfig: RemoteConfigurationEntry,
        nextVersion: String,
    ): Boolean {
        val versionResolver = remoteConfig.localEntry.versionResolveStrategy
        val currentVersion = remoteConfig.currentVersion

        return currentVersion != nextVersion &&
            versionResolver.shouldApplyNewVersion(currentVersion, nextVersion)
    }

    override fun tryUpdateSingleDirectly(
        remoteConfiguration: RemoteConfigurationEntry,
        jsonValues: String,
    ): Boolean {
        val (remoteVersion, _) = jsonValuesHelper.extractVersionFromValuesString(jsonValues)

        if (shouldApplyNewVersion(remoteConfiguration, remoteVersion)) {
            remoteConfiguration.localEntry.configurationMutator.updateValues(jsonValues)
            remoteConfiguration.currentVersion = remoteVersion
            feedbackService.postFeedbackOnConfiguration(
                remoteConfiguration.configurationId,
                remoteConfiguration.currentVersion,
            )
            logger.info("Updated ${remoteConfiguration.describe()}.")

            return true
        }

        logger.info("${remoteConfiguration.describe()} ignored version: $remoteVersion")
        return false
    }
}
