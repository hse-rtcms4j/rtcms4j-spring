package ru.enzhine.rtcms4j.spring.client.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.rtcms4j.sse.retry")
data class SseRetryConfig(
    val normalThreshold: Int = 10,
    val normalWindowSeconds: Long = 10,
    val normalBackoffBaseMs: Long = 1000, // 1 second
    val throttledThreshold: Int = 5,
    val throttledWindowSeconds: Long = 60, // 1 minute
    val throttledBackoffBaseMs: Long = 5000, // 5 seconds
    val maxBackoffMs: Long = 300_000, // 5 minutes
    val minBackoffMs: Long = 100, // 100ms
)
