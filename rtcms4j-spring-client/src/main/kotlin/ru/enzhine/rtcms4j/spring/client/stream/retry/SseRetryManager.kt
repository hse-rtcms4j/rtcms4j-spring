package ru.enzhine.rtcms4j.spring.client.stream.retry

import org.slf4j.LoggerFactory
import ru.enzhine.rtcms4j.spring.client.config.props.SseRetryConfig
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min

class SseRetryManager(
    private val sseRetryConfig: SseRetryConfig,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    enum class RetryState {
        NORMAL,
        THROTTLED,
    }

    private val retryTimestamps = mutableListOf<Instant>()
    private val retryState = AtomicReference(RetryState.NORMAL)
    private var lastRetryTime = Instant.now()

    fun shouldAttemptRetry(): Boolean {
        synchronized(retryTimestamps) {
            val now = Instant.now()

            retryTimestamps.removeAll { timestamp ->
                val cutoff =
                    when (retryState.get()) {
                        RetryState.NORMAL -> now.minusSeconds(sseRetryConfig.normalWindowSeconds)
                        RetryState.THROTTLED -> now.minusSeconds(sseRetryConfig.throttledWindowSeconds)
                    }
                timestamp.isBefore(cutoff)
            }

            val threshold =
                when (retryState.get()) {
                    RetryState.NORMAL -> sseRetryConfig.normalThreshold
                    RetryState.THROTTLED -> sseRetryConfig.throttledThreshold
                }

            val currentRetryCount = retryTimestamps.size

            return if (currentRetryCount < threshold) {
                true
            } else {
                if (retryState.get() == RetryState.NORMAL) {
                    logger.warn(
                        "Retry threshold exceeded ({} in {} seconds). Switching to throttled mode.",
                        currentRetryCount,
                        sseRetryConfig.normalWindowSeconds,
                    )
                    retryState.set(RetryState.THROTTLED)
                }
                false
            }
        }
    }

    fun recordRetryAttempt(): RetryAttempt {
        synchronized(retryTimestamps) {
            val now = Instant.now()
            retryTimestamps.add(now)
            val currentRetryCount = retryTimestamps.size

            val backoffMs = calculateBackoff(currentRetryCount)
            lastRetryTime = now

            return RetryAttempt(
                attemptNumber = currentRetryCount,
                backoffMs = backoffMs,
                state = retryState.get(),
            )
        }
    }

    private fun calculateBackoff(retryNumber: Int): Long {
        val baseMs =
            when (retryState.get()) {
                RetryState.NORMAL -> sseRetryConfig.normalBackoffBaseMs
                RetryState.THROTTLED -> sseRetryConfig.throttledBackoffBaseMs
            }

        // Exponential backoff with jitter
        val exponentialBackoff = baseMs * Math.pow(2.0, (retryNumber - 1).toDouble())

        // Cap at max backoff
        val cappedBackoff = min(exponentialBackoff.toLong(), sseRetryConfig.maxBackoffMs)

        // Add jitter (±10%)
        val jitter = (cappedBackoff * 0.1 * (Math.random() - 0.5)).toLong()

        return (cappedBackoff + jitter).coerceAtLeast(sseRetryConfig.minBackoffMs)
    }

    fun reset() {
        synchronized(retryTimestamps) {
            retryTimestamps.clear()
            retryState.set(RetryState.NORMAL)
            logger.debug("Retry state reset")
        }
    }

    fun getCurrentState(): RetryState = retryState.get()

    data class RetryAttempt(
        val attemptNumber: Int,
        val backoffMs: Long,
        val state: RetryState,
    )
}
