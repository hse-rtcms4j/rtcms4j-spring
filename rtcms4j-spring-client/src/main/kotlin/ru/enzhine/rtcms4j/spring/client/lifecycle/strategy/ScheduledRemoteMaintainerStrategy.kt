package ru.enzhine.rtcms4j.spring.client.lifecycle.strategy

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.lifecycle.strategy.ScheduledRemoteMaintainerStrategy.Companion.REMOTE_MAINTAINER_STRATEGY_SCHEDULED_NAME
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationManager
import ru.enzhine.rtcms4j.spring.client.service.RemoteConfigurationRegistry

@Component
@ConditionalOnProperty(
    prefix = "spring.rtcms4j.maintain",
    name = ["type"],
    havingValue = REMOTE_MAINTAINER_STRATEGY_SCHEDULED_NAME,
)
@EnableScheduling
class ScheduledRemoteMaintainerStrategy(
    remoteConfigurationRegistry: RemoteConfigurationRegistry,
    configurationManager: RemoteConfigurationManager,
) : OnceRemoteMaintainerStrategy(
        remoteConfigurationRegistry,
        configurationManager,
    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        const val REMOTE_MAINTAINER_STRATEGY_SCHEDULED_NAME = "scheduled"
    }

    private var started = false

    override fun maintain() {
        super.maintain()
        started = true
    }

    @ConditionalOnProperty(
        prefix = "spring.rtcms4j.maintain",
        name = [REMOTE_MAINTAINER_STRATEGY_SCHEDULED_NAME],
        matchIfMissing = true,
    )
    @Scheduled(cron = $$"${spring.rtcms4j.maintain.$$REMOTE_MAINTAINER_STRATEGY_SCHEDULED_NAME.cron}")
    fun continueMaintaining() {
        if (!started) {
            logger.warn("Ignoring scheduled maintain invocation that happened before first maintain.")
            return
        }

        super.maintain()
    }
}
