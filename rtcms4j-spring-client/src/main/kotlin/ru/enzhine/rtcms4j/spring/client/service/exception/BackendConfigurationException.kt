package ru.enzhine.rtcms4j.spring.client.service.exception

open class BackendConfigurationException(
    message: String,
    parent: Throwable? = null,
) : RuntimeException(message, parent) {
    class NotFound(
        message: String,
        parent: Throwable? = null,
    ) : BackendConfigurationException(message, parent)

    class NotMatched(
        message: String,
        parent: Throwable? = null,
    ) : BackendConfigurationException(message, parent)

    class CreationFailed(
        message: String,
        parent: Throwable? = null,
    ) : BackendConfigurationException(message, parent)

    class FetchFailed(
        message: String,
        parent: Throwable? = null,
    ) : BackendConfigurationException(message, parent)

    class CommitFailed(
        message: String,
        parent: Throwable? = null,
        val alreadyPresent: Boolean,
    ) : BackendConfigurationException(message, parent)
}
