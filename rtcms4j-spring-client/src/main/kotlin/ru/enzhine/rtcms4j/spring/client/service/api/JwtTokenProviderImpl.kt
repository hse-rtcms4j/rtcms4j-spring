package ru.enzhine.rtcms4j.spring.client.service.api

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.event.SecretRotationEvent
import ru.enzhine.rtcms4j.spring.client.service.FeedbackService
import java.time.Instant

@Service
class JwtTokenProviderImpl(
    private val keycloakClient: KeycloakClient,
    private val rtcms4jProperties: Rtcms4jProperties,
    @param:Lazy
    private val feedbackService: FeedbackService,
) : JwtTokenProvider {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var lastToken: KeycloakClient.TokenResponse? = null

    override fun getToken(): String {
        if (isTokenExpired()) {
            lastToken = keycloakClient.getKeycloakToken()
        }

        return lastToken!!.accessToken
    }

    @EventListener
    fun onConfigVersion(secretRotationEvent: SecretRotationEvent) {
        keycloakClient.rotateSecret(secretRotationEvent.newSecret)
        lastToken = null
        logger.info("Rotated client secret.")
        feedbackService.postFeedbackOnSecretRotation()
    }

    private fun isTokenExpired(): Boolean {
        val expiresAt = lastToken?.expiresAt
        if (expiresAt == null) {
            return true
        }

        return expiresAt
            .minus(rtcms4jProperties.tokenRefreshOffset)
            .isBefore(Instant.now())
    }
}
