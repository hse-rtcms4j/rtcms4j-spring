package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.rtcms4j.api")
data class ApiProperties(
    val baseUrl: String?,
    val coreBaseUrl: String?,
    val notifyBaseUrl: String?,
)
