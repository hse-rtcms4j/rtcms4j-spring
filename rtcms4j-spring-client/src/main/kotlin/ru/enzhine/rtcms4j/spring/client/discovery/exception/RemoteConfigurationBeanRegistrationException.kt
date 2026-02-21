package ru.enzhine.rtcms4j.spring.client.discovery.exception

class RemoteConfigurationBeanRegistrationException(
    message: String,
    parent: Throwable? = null,
) : RuntimeException(message, parent)
