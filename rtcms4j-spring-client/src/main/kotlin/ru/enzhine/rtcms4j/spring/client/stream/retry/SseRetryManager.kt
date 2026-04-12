package ru.enzhine.rtcms4j.spring.client.stream.retry

import org.slf4j.LoggerFactory
import ru.enzhine.rtcms4j.spring.client.config.props.SseRetryConfig
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class SseRetryManager(
    private val sseRetryConfig: SseRetryConfig,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val attemptCounter = AtomicInteger(0)
    private val maxAttempts = sseRetryConfig.threshold

    fun shouldAttemptRetry(): Boolean {
        val currentCount = attemptCounter.get()
        return currentCount < maxAttempts
    }

    fun recordRetryAttempt(): RetryAttempt {
        val attemptNumber = attemptCounter.incrementAndGet()

        if (attemptNumber > maxAttempts) {
            logger.warn("Retry limit exceeded (max: {}). Retries stopped until reset.", maxAttempts)
        }

        val backoffMs = calculateBackoff(attemptNumber)

        return RetryAttempt(
            attemptNumber = attemptNumber,
            backoffMs = backoffMs,
        )
    }

    private fun calculateBackoff(attemptNumber: Int): Long {
        val baseMs = sseRetryConfig.backoffBaseMs
        val maxBackoff = sseRetryConfig.maxBackoffMs
        val minBackoff = sseRetryConfig.minBackoffMs

        val exponentialMs = baseMs * (1L shl (attemptNumber - 1))
        val cappedMs = min(exponentialMs, maxBackoff)

        val jitter = (cappedMs * 0.1 * (Math.random() - 0.5)).toLong()

        return (cappedMs + jitter).coerceAtLeast(minBackoff)
    }

    fun reset() {
        val previousCount = attemptCounter.getAndSet(0)
        if (previousCount > 0) {
            logger.debug("Retry manager reset after {} attempts", previousCount)
        }
    }

    data class RetryAttempt(
        val attemptNumber: Int,
        val backoffMs: Long,
    )
}
