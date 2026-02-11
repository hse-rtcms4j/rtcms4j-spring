package ru.enzhine.rtcms4j.spring.client.service.exception

class BackendConfigurationNotMatchedException(
    message: String,
    parent: Throwable?,
) : RuntimeException(message, parent)
