package ru.enzhine.rtcms4j.example.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration

@RemoteConfiguration(version = "1.0.0")
@ConfigurationProperties("feature-config")
open class FeatureConfig(
    open val helloWhom: String,
)
