package ru.enzhine.rtcms4j.spring.client.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import ru.enzhine.rtcms4j.notify.api.NotifyApi
import ru.enzhine.rtcms4j.notify.api.dto.ApplicationFeedbackRequestDto
import ru.enzhine.rtcms4j.notify.api.dto.ConfigurationFeedbackRequestDto
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties

@Service
class FeedbackServiceImpl(
    private val notifyApi: NotifyApi,
    private val rtcms4jProperties: Rtcms4jProperties,
) : FeedbackService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postFeedbackOnConfiguration(
        configId: Long,
        version: String,
    ) {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId
        val cid = configId

        val request =
            ConfigurationFeedbackRequestDto().apply {
                clientName = rtcms4jProperties.clientName
                appliedVersion = version
            }

        try {
            notifyApi.postConfigurationFeedback(nid, aid, cid, request)
        } catch (ex: RestClientResponseException) {
            logger.error("Error posting configuration feedback.", ex)
        }
    }

    override fun postFeedbackOnSecretRotation() {
        val nid = rtcms4jProperties.namespaceId
        val aid = rtcms4jProperties.applicationId

        val request =
            ApplicationFeedbackRequestDto().apply {
                clientName = rtcms4jProperties.clientName
                isSecretRotated = true
            }

        try {
            notifyApi.postApplicationFeedback(nid, aid, request)
        } catch (ex: RestClientResponseException) {
            logger.error("Error posting application feedback.", ex)
        }
    }
}
