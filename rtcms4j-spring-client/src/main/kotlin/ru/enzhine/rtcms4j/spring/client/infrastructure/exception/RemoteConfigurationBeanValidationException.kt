package ru.enzhine.rtcms4j.spring.client.infrastructure.exception

class RemoteConfigurationBeanValidationException(
    message: String,
    parent: Throwable?,
) : RuntimeException(message, parent)
