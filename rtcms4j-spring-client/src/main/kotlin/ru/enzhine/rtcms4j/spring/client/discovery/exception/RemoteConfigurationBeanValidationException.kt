package ru.enzhine.rtcms4j.spring.client.discovery.exception

class RemoteConfigurationBeanValidationException(
    message: String,
    parent: Throwable?,
) : RuntimeException(message, parent)
