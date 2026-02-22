package ru.enzhine.rtcms4j.example.config.props

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.annotation.RemoteConfiguration

@RemoteConfiguration(version = "1.1.0")
@Component
class SumProperties(
    @field:JsonPropertyDescription("Minimal allowed 'a' value.")
    val minA: Double = -10.0,
    @field:JsonPropertyDescription("Maximum allowed 'a' value.")
    val maxA: Double = 10.0,
    @field:JsonPropertyDescription("Minimal allowed 'b' value.")
    val minB: Double = -10.0,
    @field:JsonPropertyDescription("Maximum allowed 'b' value.")
    val maxB: Double = 10.0,
    @field:JsonPropertyDescription("Minimal allowed 'c' value.")
    val minC: Double = -10.0,
    @field:JsonPropertyDescription("Maximum allowed 'c' value.")
    val maxC: Double = 10.0,
    @field:JsonPropertyDescription("Whether to permit calculation of negative values.")
    val allowNegative: Boolean = true,
    @field:JsonPropertyDescription("Templated message that client receives when invokes sum operation.")
    val returnTemplate: String = "The sum of %s, %s, and %s is equal to %s.",
    @field:JsonPropertyDescription("Templated message that client receives when an error occurs.")
    val errorMessage: String = "Error: %s",
)
