package ru.enzhine.rtcms4j.spring.client.sse.event

data class SecretRotationEvent(
    val newSecret: String,
)
