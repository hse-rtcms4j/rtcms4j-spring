package ru.enzhine.rtcms4j.spring.client.lifecycle

import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationManager
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationRegistry
import java.util.concurrent.atomic.AtomicBoolean

@Component
class RemoteConfigurationOperator(
    private val remoteConfigurationRegistry: RemoteConfigurationRegistry,
    private val configurationManager: RemoteConfigurationManager,
) : SmartLifecycle {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private val isInitialized = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(false)

    override fun getPhase(): Int = Int.MAX_VALUE - 1_000

    override fun isAutoStartup(): Boolean = true

    override fun start() =
        if (isInitialized.compareAndSet(false, true)) {
            logger.info("Starting remote configuration operator...")

            try {
                initialize()

                isRunning.set(true)
                logger.info("Remote configuration operator started!")
            } catch (ex: Exception) {
                logger.error("An error occurred during remote configuration operator init.", ex)
            }
        } else {
            Unit
        }

    override fun stop() =
        if (isInitialized.compareAndSet(true, false)) {
            logger.info("Stopping remote configuration operator...")
            isRunning.set(false)
        } else {
            Unit
        }

    override fun isRunning(): Boolean = isRunning.get()

    private fun initialize() {
        remoteConfigurationRegistry.entries().forEach {
            configurationManager.fetchRemoteAndUpdate(it)
        }
    }
}
