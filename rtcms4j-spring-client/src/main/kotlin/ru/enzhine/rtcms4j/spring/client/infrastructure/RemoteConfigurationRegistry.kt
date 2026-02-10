package ru.enzhine.rtcms4j.spring.client.infrastructure

import ru.enzhine.rtcms4j.spring.client.infrastructure.dto.RemoteConfigurationEntry

interface RemoteConfigurationRegistry {
    fun register(entry: RemoteConfigurationEntry)

    fun entries(): List<RemoteConfigurationEntry>
}
