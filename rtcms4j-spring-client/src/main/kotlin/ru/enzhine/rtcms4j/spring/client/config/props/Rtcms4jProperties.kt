package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import java.util.UUID

@ConfigurationProperties(prefix = "spring.rtcms4j")
data class Rtcms4jProperties(
    val enabled: Boolean = true,
    val namespaceId: Long,
    val applicationId: Long,
    val tokenRefreshOffset: Duration,
    val clientName: String = UUID.randomUUID().toString(),
    val pageSize: Long = 20,
)
