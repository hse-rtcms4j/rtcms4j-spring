package ru.enzhine.rtcms4j.spring.client.service

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.context.annotation.Role
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationCommitRequest
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDetailedDto
import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDtoCreateRequest
import ru.enzhine.rtcms4j.core.api.dto.SourceType
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.discovery.mutator.ConfigurationMutator
import ru.enzhine.rtcms4j.spring.client.mapper.toService
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendConfigurationEntry
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendState
import ru.enzhine.rtcms4j.spring.client.service.exception.BackendConfigurationException

@Role(ROLE_INFRASTRUCTURE)
@Component
class BackendConfigurationServiceImpl(
    private val rtcms4jProperties: Rtcms4jProperties,
    private val coreApi: CoreApi,
) : BackendConfigurationService {
    override fun getBackendConfigurations(): List<BackendConfigurationEntry> {
        val result = mutableListOf<BackendConfigurationEntry>()

        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val pageSize = rtcms4jProperties.pageSize
        var currentPage = 0
        var totalPages: Long
        do {
            val pagedModel =
                try {
                    coreApi.findAllConfigurations(nid, aid, null, currentPage, pageSize)!!
                } catch (ex: RestClientResponseException) {
                    throw BackendConfigurationException.FetchFailed(
                        message = "No backend configurations accessible.",
                        parent = ex,
                    )
                }

            pagedModel.content.forEach {
                result.add(
                    BackendConfigurationEntry(
                        configurationName = it.name,
                        configurationId = it.id,
                        version = it.commitVersion,
                    ),
                )
            }
            totalPages = pagedModel.page.totalPages
        } while (currentPage++ < totalPages)

        return result
    }

    override fun createNewRemote(configurationName: String): ConfigurationDetailedDto =
        try {
            val nid = rtcms4jProperties.namespaceId
            val aid = rtcms4jProperties.applicationId
            val request =
                ConfigurationDtoCreateRequest().apply {
                    name = configurationName
                    schemaSourceType = SourceType.SERVICE
                }

            coreApi.createConfiguration(nid, aid, request)
        } catch (ex: RestClientResponseException) {
            throw BackendConfigurationException.CreationFailed(
                message = "Remote-configuration '$configurationName' creation failed.",
                parent = ex,
            )
        }

    override fun fetchRemote(configurationId: Long): BackendState? =
        try {
            val nid = rtcms4jProperties.namespaceId
            val aid = rtcms4jProperties.applicationId

            coreApi.getConfiguration(nid, aid, configurationId).toService()
        } catch (ex: RestClientResponseException) {
            throw BackendConfigurationException.FetchFailed(
                message = "Remote-configuration with id '$configurationId' not found.",
                parent = ex,
            )
        }

    override fun commitToRemote(
        configurationId: Long,
        configurationMutator: ConfigurationMutator,
        version: String,
    ): ConfigurationCommitDetailedDto =
        try {
            val nid = rtcms4jProperties.namespaceId
            val aid = rtcms4jProperties.applicationId
            val request =
                ConfigurationCommitRequest().apply {
                    jsonSchema = configurationMutator.getJsonSchema()
                    jsonValues = configurationMutator.getJsonValuesWithVersion(version)
                }

            coreApi.commitConfiguration(nid, aid, configurationId, request)
        } catch (ex: RestClientResponseException) {
            val alreadyPresent = ex.statusCode == HttpStatus.CONFLICT
            throw BackendConfigurationException.CommitFailed(
                message = "Remote-configuration with id '$configurationId' commit failed.",
                parent = ex,
                alreadyPresent = alreadyPresent,
            )
        }
}
