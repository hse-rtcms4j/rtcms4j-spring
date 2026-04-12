package ru.enzhine.rtcms4j.spring.client.lifecycle.strategy

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.lifecycle.strategy.OnceRemoteMaintainerStrategy.Companion.REMOTE_MAINTAINER_STRATEGY_ONCE_NAME
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationManager
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationRegistry

@Component
@ConditionalOnProperty(
    prefix = "spring.rtcms4j.maintain",
    name = ["type"],
    havingValue = REMOTE_MAINTAINER_STRATEGY_ONCE_NAME,
)
open class OnceRemoteMaintainerStrategy(
    protected val remoteConfigurationRegistry: RemoteConfigurationRegistry,
    protected val configurationManager: RemoteConfigurationManager,
) : RemoteMaintainerStrategy {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        const val REMOTE_MAINTAINER_STRATEGY_ONCE_NAME = "once"
    }

    override fun maintain() {
        val remoteConfigurations = remoteConfigurationRegistry.entries()
        val updatedCount = configurationManager.tryUpdateMultipleAuto(remoteConfigurations)
        logger.info("[$updatedCount/${remoteConfigurations.size}] remote-configurations updated.")
    }
}
