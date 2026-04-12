package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.rtcms4j.maintain.stream")
data class SseRetryConfig(
    val threshold: Int = 10,
    val backoffBaseMs: Long = 1000,
    val minBackoffMs: Long = 100,
    val maxBackoffMs: Long = 1000 * 30,
)
