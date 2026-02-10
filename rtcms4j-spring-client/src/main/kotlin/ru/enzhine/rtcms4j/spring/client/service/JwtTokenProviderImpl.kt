package ru.enzhine.rtcms4j.spring.client.service

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import java.time.Instant

@Service
class JwtTokenProviderImpl(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
    clientRegistrationRepository: ClientRegistrationRepository,
    private val rtcms4jProperties: Rtcms4jProperties,
) : JwtTokenProvider {
    companion object {
        const val CLIENT_REGISTRATION_ID = "rtcms4j-client"
    }

    init {
        val clientRegistration =
            clientRegistrationRepository.findByRegistrationId(CLIENT_REGISTRATION_ID)
        if (clientRegistration == null) {
            throw RuntimeException("Keycloak client '$CLIENT_REGISTRATION_ID' not configured.")
        }
    }

    private var lastToken: OAuth2AccessToken? = null

    private fun fetchToken(): OAuth2AccessToken {
        val authorizeRequest =
            OAuth2AuthorizeRequest
                .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                .principal(CLIENT_REGISTRATION_ID)
                .build()

        val authorizedClient = authorizedClientManager.authorize(authorizeRequest)
        if (authorizedClient == null) {
            throw RuntimeException("Failed to authorize '$CLIENT_REGISTRATION_ID'.")
        }

        return authorizedClient.accessToken
    }

    override fun getToken(): String {
        if (isTokenExpired()) {
            lastToken = fetchToken()
        }

        return lastToken!!.tokenValue
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
