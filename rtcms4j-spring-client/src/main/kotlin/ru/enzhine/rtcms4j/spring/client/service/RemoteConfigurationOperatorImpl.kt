package ru.enzhine.rtcms4j.spring.client.service

import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.core.api.dto.Pageable
import ru.enzhine.rtcms4j.notify.api.NotifyApi
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.infrastructure.RemoteConfigurationRegistry
import ru.enzhine.rtcms4j.spring.client.infrastructure.dto.RemoteConfigurationEntry

@Service
class RemoteConfigurationOperatorImpl(
    private val remoteConfigurationRegistry: RemoteConfigurationRegistry,
    private val rtcms4jProperties: Rtcms4jProperties,
    private val coreApi: CoreApi,
    private val notifyApi: NotifyApi,
) : RemoteConfigurationOperator {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @EventListener
    fun handleContextRefreshed(event: ContextRefreshedEvent) {
        logger.info("Registered entries:")
        remoteConfigurationRegistry.entries().forEach { entry ->
            logger.info(entry.toString())
            sync(entry)
        }
    }

    override fun sync(remoteConfigurationEntry: RemoteConfigurationEntry) {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val pageable =
            Pageable().apply {
                page = 1
                size = 20
            }

        val pagedModel = coreApi.findAllConfigurations(nid, aid, null, pageable)
    }
}
