package ru.enzhine.rtcms4j.example.config.props

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration

@RemoteConfiguration(version = "1.1.0")
@Component
class FeatureConfig(
    @field:JsonPropertyDescription("Whom to greet, when accessing page.")
    val helloWhom: String = "World",
    @field:JsonPropertyDescription("Chance to show easter egg.")
    val secretChance: Double = 0.5,
)
