package ru.enzhine.rtcms4j.spring.client.service.dto

data class BackendConfigurationEntry(
    val configurationName: String,
    val configurationId: Long,
    var version: String?,
)
