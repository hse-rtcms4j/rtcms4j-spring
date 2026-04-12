package ru.enzhine.rtcms4j.spring.client.lifecycle

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.lifecycle.strategy.RemoteMaintainerStrategy
import java.util.concurrent.atomic.AtomicBoolean

@Component
class RemoteConfigurationOperator(
    private val remoteMaintainerStrategy: RemoteMaintainerStrategy,
) : SmartLifecycle,
    HealthIndicator {
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
        logger.info("Using strategy: ${remoteMaintainerStrategy::class.java.simpleName}.")
        remoteMaintainerStrategy.maintain()
    }

    override fun health(): Health =
        if (isRunning()) {
            Health
                .up()
                .withDetail("module", "RemoteConfigurationOperator")
                .withDetail("status", "running")
                .build()
        } else {
            Health
                .down()
                .withDetail("module", "RemoteConfigurationOperator")
                .withDetail("status", "stopped")
                .build()
        }
}
