package ru.enzhine.rtcms4j.spring.client.event

data class ConfigurationVersionEvent(
    val configurationId: Long,
    val content: String,
)
