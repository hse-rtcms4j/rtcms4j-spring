package ru.enzhine.rtcms4j.spring.client.sse.exception

open class SseException(
    message: String,
    parent: Throwable? = null,
) : RuntimeException(message, parent) {
    class ExecutionFailed(
        message: String,
        parent: Throwable? = null,
    ) : SseException(message, parent)

    class Interrupted(
        message: String,
        parent: Throwable? = null,
    ) : SseException(message, parent)
}
