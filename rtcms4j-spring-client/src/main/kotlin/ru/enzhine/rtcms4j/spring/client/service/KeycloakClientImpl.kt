package ru.enzhine.rtcms4j.spring.client.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import ru.enzhine.rtcms4j.spring.client.config.props.KeycloakProperties

@Service
class KeycloakClientImpl(
    private val keycloakProperties: KeycloakProperties,
) : KeycloakClient {
    private val restClient = RestClient.create()

    private var actualSecret = keycloakProperties.clientSecret

    override fun getKeycloakToken(): KeycloakClient.TokenResponse =
        restClient
            .post()
            .uri(buildTokenUrl(keycloakProperties))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                "grant_type=client_credentials&" +
                    "client_id=${keycloakProperties.clientId}&" +
                    "client_secret=$actualSecret",
            ).retrieve()
            .body(KeycloakClient.TokenResponse::class.java)
            ?: throw RuntimeException("KeycloakClient unknown response (null).")

    private fun buildTokenUrl(keycloakProperties: KeycloakProperties) =
        "${keycloakProperties.serverUrl}/realms/${keycloakProperties.realm}/protocol/openid-connect/token"

    override fun rotateSecret(newSecret: String) {
        actualSecret = newSecret
    }
}
