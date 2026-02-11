package ru.enzhine.rtcms4j.spring.client.service

import org.springframework.stereotype.Service
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import java.time.Instant

@Service
class JwtTokenProviderImpl(
    private val keycloakClient: KeycloakClient,
    private val rtcms4jProperties: Rtcms4jProperties,
) : JwtTokenProvider {
    private var lastToken: KeycloakClient.TokenResponse? = null

    override fun getToken(): String {
        if (isTokenExpired()) {
            lastToken = keycloakClient.getKeycloakToken()
        }

        return lastToken!!.accessToken
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
