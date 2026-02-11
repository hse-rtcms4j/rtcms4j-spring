package ru.enzhine.rtcms4j.spring.client.service

import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry

interface RemoteConfigurationManager {
    fun fetchRemoteAndUpdate(remoteConfigurationEntry: RemoteConfigurationEntry)
}
