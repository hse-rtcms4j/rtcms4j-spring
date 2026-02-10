package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.spring.client.infrastructure.dto.RemoteConfigurationEntry

interface RemoteConfigurationOperator {
    fun sync(remoteConfigurationEntry: RemoteConfigurationEntry)
}
