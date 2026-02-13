package ru.enzhine.rtcms4j.spring.client.service

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.core.api.dto.Pageable
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry

@Role(ROLE_INFRASTRUCTURE)
@Component
class BackendConfigurationProviderImpl(
    private val rtcms4jProperties: Rtcms4jProperties,
    private val coreApi: CoreApi,
) : BackendConfigurationProvider {
    private val backendEntries = mutableListOf<BackendConfigurationEntry>()

    override fun getBackendConfigurations(): List<BackendConfigurationEntry> {
        if (backendEntries.isNotEmpty()) {
            return backendEntries
        }

        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val pageSize = rtcms4jProperties.pageSize
        var currentPage = 0L
        var totalPages: Long
        do {
            val pageable =
                Pageable().apply {
                    this.page = currentPage
                    this.size = pageSize
                }

            val pagedModel = coreApi.findAllConfigurations(nid, aid, null, pageable)!!
            pagedModel.content.forEach {
                backendEntries.add(
                    BackendConfigurationEntry(
                        configName = it.name,
                        configId = it.id,
                        version = it.commitVersion,
                    ),
                )
            }
            totalPages = pagedModel.page.totalPages
        } while (currentPage++ < totalPages)

        return backendEntries
    }

    override fun evictBackendConfigurations() {
        backendEntries.clear()
    }
}
