package ru.enzhine.rtcms4j.example.config.props

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration

@RemoteConfiguration(version = "1.1.2")
@Component
data class TestConfig(
    @field:JsonPropertyDescription("Names to which display correct message")
    var names: List<String> = listOf("Onar"),
)
