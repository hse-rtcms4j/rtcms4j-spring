package ru.enzhine.rtcms4j.spring.client.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.notify.api.NotifyApi
import ru.enzhine.rtcms4j.spring.client.config.props.ApiProperties
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.service.JwtTokenProvider

@EnableConfigurationProperties(
    ApiProperties::class,
    Rtcms4jProperties::class,
)
@Configuration
class Rtcms4jConfig {
    @Bean
    fun oAuth2AuthorizedClientService(clientRegistrationRepository: ClientRegistrationRepository) =
        InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .refreshToken()
                .build()

        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            ).apply {
                setAuthorizedClientProvider(authorizedClientProvider)
            }

        return authorizedClientManager
    }

    @Bean
    fun coreApi(
        coreApiProperties: ApiProperties,
        jwtTokenProvider: JwtTokenProvider,
    ) = CoreApi().apply {
        apiClient.basePath = coreApiProperties.coreBaseUrl
        apiClient.setBearerToken(jwtTokenProvider::getToken)
    }

    @Bean
    fun notifyApi(
        coreApiProperties: ApiProperties,
        jwtTokenProvider: JwtTokenProvider,
    ) = NotifyApi().apply {
        apiClient.basePath = coreApiProperties.notifyBaseUrl
        apiClient.setBearerToken(jwtTokenProvider::getToken)
    }
}
