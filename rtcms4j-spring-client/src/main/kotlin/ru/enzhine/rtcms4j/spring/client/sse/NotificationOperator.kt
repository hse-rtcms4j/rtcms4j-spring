package ru.enzhine.rtcms4j.spring.client.sse

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.notify.api.dto.NotificationEventDto
import ru.enzhine.rtcms4j.spring.client.sse.event.ConfigurationVersionEvent
import ru.enzhine.rtcms4j.spring.client.sse.event.SecretRotationEvent
import ru.enzhine.rtcms4j.spring.client.sse.event.StreamInterruptedEvent
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

@Component
class NotificationOperator(
    private val notificationClient: NotificationClient,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : SmartLifecycle {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

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

                isRunning.set(true)
            } catch (ex: Throwable) {
                logger.error("RTCMS4J notifications connection failed.", ex)
            }
        } else {
            Unit
        }

    override fun stop() =
        if (isInitialized.compareAndSet(true, false)) {
            logger.info("Disabling RTCMS4J notifications connection...")
            closeConnection()
            isRunning.set(false)
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
    }

    private fun interrupter(inputStream: InputStream) {
        this.connection = inputStream
    }

    private fun onNotification(notification: NotificationEventDto) {
        when {
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

                applicationEventPublisher.publishEvent(
                    SecretRotationEvent(
                        newSecret = event.newPassword,
                    ),
                )
            }

            else -> {
                logger.warn("Unknown notification: $notification")
            }
        }
    }

    private fun onError(throwable: Throwable) {
        // TODO: exponential backoff
        if (isInitialized.get()) {
            logger.error("Notification SSE connection was interrupted.", throwable)
            closeConnection()
            applicationEventPublisher.publishEvent(StreamInterruptedEvent())

            logger.info("Reattempting RTCMS4J notifications connection...")
            initializeConnection()
        }
    }

    private fun closeConnection() =
        try {
            val con = connection
            con?.close()
            Unit
        } catch (ignored: Throwable) {
        }
}
