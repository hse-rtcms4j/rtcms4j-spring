package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.rtcms4j.keycloak")
data class KeycloakProperties(
    val serverUrl: String,
    val realm: String = "rtcms4j",
    val clientId: String,
    val clientSecret: String,
)
