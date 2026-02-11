package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "spring.rtcms4j")
data class Rtcms4jProperties(
    val enabled: Boolean = true,
    val namespaceId: Long,
    val applicationId: Long,
    val tokenRefreshOffset: Duration,
    val pageSize: Long = 20,
)
