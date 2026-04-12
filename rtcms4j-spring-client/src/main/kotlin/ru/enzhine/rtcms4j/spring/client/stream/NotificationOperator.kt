package ru.enzhine.rtcms4j.spring.client.stream

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.notify.api.dto.NotificationEventDto
import ru.enzhine.rtcms4j.spring.client.config.props.SseRetryConfig
import ru.enzhine.rtcms4j.spring.client.event.ConfigurationVersionEvent
import ru.enzhine.rtcms4j.spring.client.event.SecretRotationEvent
import ru.enzhine.rtcms4j.spring.client.event.StreamInterruptedEvent
import ru.enzhine.rtcms4j.spring.client.lifecycle.strategy.StreamRemoteMaintainerStrategy
import ru.enzhine.rtcms4j.spring.client.stream.api.NotificationClient
import ru.enzhine.rtcms4j.spring.client.stream.retry.SseRetryManager
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

@Component
@ConditionalOnBean(StreamRemoteMaintainerStrategy::class)
class NotificationOperator(
    private val notificationClient: NotificationClient,
    private val applicationEventPublisher: ApplicationEventPublisher,
    sseRetryConfig: SseRetryConfig,
) : SmartLifecycle,
    HealthIndicator {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private val sseRetryManager = SseRetryManager(sseRetryConfig)

    private val isInitialized = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(false)

    private var sseThread: Thread? = null
    private var connection: InputStream? = null

    override fun isAutoStartup(): Boolean = true

    override fun start() =
        if (isInitialized.compareAndSet(false, true)) {
            logger.info("Initiating RTCMS4J notifications connection...")

            try {
                initializeConnection()
            } catch (ex: Throwable) {
                logger.error("RTCMS4J notifications connection failed.", ex)
            }
        } else {
            Unit
        }

    override fun stop() =
        if (isInitialized.compareAndSet(true, false)) {
            closeConnection()
            isRunning.set(false)
            logger.info("RTCMS4J notifications connection disabled.")
        } else {
            Unit
        }

    override fun isRunning(): Boolean = isRunning.get()

    private fun initializeConnection() {
        sseThread =
            Thread
                .ofVirtual()
                .name("rtcms4j-sse")
                .start {
                    notificationClient.subscribeOnNotificationSse(
                        interrupter = this::interrupter,
                        onNotification = this::onNotification,
                        onError = this::onError,
                    )
                }
        isRunning.set(true)
    }

    private fun interrupter(inputStream: InputStream) {
        this.connection = inputStream
    }

    private fun onNotification(notification: NotificationEventDto) {
        sseRetryManager.reset()
        when {
            notification.isHeartbeat -> Unit

            notification.configurationUpdateEvent != null -> {
                val event = notification.configurationUpdateEvent

                applicationEventPublisher.publishEvent(
                    ConfigurationVersionEvent(
                        configurationId = event.configurationId,
                        content = event.content,
                    ),
                )
            }

            notification.passwordRotationEvent != null -> {
                val event = notification.passwordRotationEvent
                event.newPassword?.let {
                    applicationEventPublisher.publishEvent(SecretRotationEvent(it))
                }
            }

            else -> {
                logger.warn("Unknown notification: $notification")
            }
        }
    }

    private fun onError(throwable: Throwable) {
        if (isInitialized.get()) {
            isRunning.set(false)
            logger.error("Notification SSE connection was interrupted.", throwable)
            closeConnection()
            try {
                applicationEventPublisher.publishEvent(StreamInterruptedEvent())
            } catch (throwable: Throwable) {
                logger.error("An error occurred during interruption event publishing.", throwable)
            }

            if (sseRetryManager.shouldAttemptRetry()) {
                scheduleRetry()
            } else {
                logger.error("Max retry attempts reached. Stopping reconnection attempts.")
                stop()
            }
        }
    }

    private fun scheduleRetry() {
        val attempt = sseRetryManager.recordRetryAttempt()

        logger.info(
            "Connection retry attempt ${attempt.attemptNumber}. " +
                "Waiting ${attempt.backoffMs} ms before next reconnection attempt.",
        )

        Thread.startVirtualThread {
            try {
                Thread.sleep(attempt.backoffMs)
                if (isInitialized.get()) {
                    logger.info("Executing scheduled reconnection attempt...")
                    initializeConnection()
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.debug("Retry sleep was interrupted")
            }
        }
    }

    private fun closeConnection() =
        try {
            val con = connection
            con?.close()
            Unit
        } catch (ignored: Throwable) {
        }

    override fun health(): Health =
        if (isRunning()) {
            Health
                .up()
                .withDetail("module", "NotificationOperator")
                .withDetail("status", "running")
                .build()
        } else {
            Health
                .down()
                .withDetail("module", "NotificationOperator")
                .withDetail("status", "stopped")
                .build()
        }
}
