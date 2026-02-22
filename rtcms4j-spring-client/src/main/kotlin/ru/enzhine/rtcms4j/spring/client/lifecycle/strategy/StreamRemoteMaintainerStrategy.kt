package ru.enzhine.rtcms4j.spring.client.lifecycle.strategy

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.event.ConfigurationVersionEvent
import ru.enzhine.rtcms4j.spring.client.event.StreamInterruptedEvent
import ru.enzhine.rtcms4j.spring.client.lifecycle.strategy.StreamRemoteMaintainerStrategy.Companion.REMOTE_MAINTAINER_STRATEGY_STREAM_NAME
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationManager
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationRegistry
import ru.enzhine.rtcms4j.spring.client.stream.NotificationOperator

@ComponentScan(basePackageClasses = [NotificationOperator::class])
@Component
@ConditionalOnProperty(
    prefix = "spring.rtcms4j.maintain",
    name = ["type"],
    havingValue = REMOTE_MAINTAINER_STRATEGY_STREAM_NAME,
    matchIfMissing = true,
)
class StreamRemoteMaintainerStrategy(
    remoteConfigurationRegistry: RemoteConfigurationRegistry,
    configurationManager: RemoteConfigurationManager,
) : OnceRemoteMaintainerStrategy(
        remoteConfigurationRegistry,
        configurationManager,
    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        const val REMOTE_MAINTAINER_STRATEGY_STREAM_NAME = "stream"
    }

    private var started = false

    override fun maintain() {
        super.maintain()
        started = true
    }

    @EventListener
    fun onConfigVersion(configurationVersionEvent: ConfigurationVersionEvent) {
        if (!started) {
            logger.warn("Ignoring ConfigurationVersionEvent that appeared before first maintain.")
            return
        }

        val remoteId = configurationVersionEvent.configurationId
        val matched =
            remoteConfigurationRegistry.entries().find { it.configurationId == remoteId }
                ?: run {
                    logger.info("Ignoring unknown remote-configuration with id=$remoteId.")
                    return
                }

        val content = configurationVersionEvent.content
        configurationManager.tryUpdateSingleDirectly(matched, content)
    }

    @EventListener
    fun onStreamInterrupted(streamInterruptedEvent: StreamInterruptedEvent) {
        if (!started) {
            logger.warn("Ignoring StreamInterruptedEvent that appeared before first maintain.")
            return
        }

        super.maintain()
    }
}
