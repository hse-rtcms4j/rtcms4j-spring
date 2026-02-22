package ru.enzhine.rtcms4j.spring.client.version.exception

class RemoteConfigurationVersionException(
    message: String,
    parent: Throwable? = null,
) : RuntimeException(message, parent)
