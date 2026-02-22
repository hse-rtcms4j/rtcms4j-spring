package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.rtcms4j.features")
data class FeaturesProperties(
    val ignoreConfigurationOnMatchFailure: Boolean = false,
    val ignoreConfigurationOnCreationFailure: Boolean = false,
    val skipConfigurationOnRemoteLostFailure: Boolean = true,
    val skipConfigurationOnVersionFailure: Boolean = true,
    val skipConfigurationOnCommitFailure: Boolean = true,
    val skipConfigurationOnFetchFailure: Boolean = true,
)
