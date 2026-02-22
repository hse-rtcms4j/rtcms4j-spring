package ru.enzhine.rtcms4j.spring.client.event

data class SecretRotationEvent(
    val newSecret: String,
)
