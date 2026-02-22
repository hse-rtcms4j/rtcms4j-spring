package ru.enzhine.rtcms4j.spring.client.service.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

interface KeycloakClient {
    fun getKeycloakToken(): TokenResponse

    fun rotateSecret(newSecret: String)

    data class TokenResponse(
        @field:JsonProperty("access_token")
        val accessToken: String,
        @field:JsonProperty("expires_in")
        val expiresIn: Long,
        @field:JsonProperty("token_type")
        val tokenType: String,
        @field:JsonProperty("scope")
        val scope: String? = null,
    ) {
        val expiresAt = Instant.now().plusSeconds(expiresIn)
    }
}
