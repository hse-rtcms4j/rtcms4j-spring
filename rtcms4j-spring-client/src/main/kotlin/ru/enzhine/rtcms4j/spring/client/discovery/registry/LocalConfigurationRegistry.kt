package ru.enzhine.rtcms4j.spring.client.discovery.registry

import ru.enzhine.rtcms4j.spring.client.discovery.registry.dto.LocalConfigurationEntry

interface LocalConfigurationRegistry {
    fun register(entry: LocalConfigurationEntry)

    fun entries(): List<LocalConfigurationEntry>
}
