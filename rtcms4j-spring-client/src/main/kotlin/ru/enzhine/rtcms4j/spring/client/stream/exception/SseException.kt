package ru.enzhine.rtcms4j.spring.client.stream.exception

import org.springframework.http.HttpStatusCode

open class SseException(
    message: String,
    parent: Throwable? = null,
) : RuntimeException(message, parent) {
    class ExecutionFailed(
        message: String,
        val statusCode: HttpStatusCode,
        parent: Throwable? = null,
    ) : SseException(message, parent)

    class Interrupted(
        message: String,
        parent: Throwable? = null,
    ) : SseException(message, parent)
}
