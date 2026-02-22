package ru.enzhine.rtcms4j.spring.client.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.enzhine.rtcms4j.core.api.CoreApi
import ru.enzhine.rtcms4j.notify.api.NotifyApi
import ru.enzhine.rtcms4j.spring.client.config.props.ApiProperties
import ru.enzhine.rtcms4j.spring.client.config.props.FeaturesProperties
import ru.enzhine.rtcms4j.spring.client.config.props.KeycloakProperties
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.config.props.SseRetryConfig
import ru.enzhine.rtcms4j.spring.client.service.api.JwtTokenProvider

@EnableConfigurationProperties(
    Rtcms4jProperties::class,
    FeaturesProperties::class,
    ApiProperties::class,
    KeycloakProperties::class,
    SseRetryConfig::class,
)
@Configuration
class Rtcms4jConfig {
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
