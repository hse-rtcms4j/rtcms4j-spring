package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry

interface BackendConfigurationProvider {
    fun getBackendConfigurations(): List<BackendConfigurationEntry>

    fun evictBackendConfigurations()
}
