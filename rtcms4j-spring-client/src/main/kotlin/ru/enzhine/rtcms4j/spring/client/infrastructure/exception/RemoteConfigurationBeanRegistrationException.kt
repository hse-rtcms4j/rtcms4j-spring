package ru.enzhine.rtcms4j.spring.client.infrastructure.exception

class RemoteConfigurationBeanRegistrationException(
    message: String,
    parent: Throwable?,
) : RuntimeException(message, parent)
