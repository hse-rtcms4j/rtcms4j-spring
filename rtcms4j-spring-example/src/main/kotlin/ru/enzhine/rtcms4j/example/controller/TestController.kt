package ru.enzhine.rtcms4j.example.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.enzhine.rtcms4j.example.config.props.FeatureConfig
import ru.enzhine.rtcms4j.example.config.props.SumProperties
import ru.enzhine.rtcms4j.example.config.props.TestConfig

@RestController
class TestController(
    private val featureConfig: FeatureConfig,
    private val testConfig: TestConfig,
    private val sumProperties: SumProperties,
) {
    @GetMapping("/hello")
    fun hello(): String {
        if (Math.random() < featureConfig.secretChance) {
            return "I always come back!"
        }
        val whom = featureConfig.helloWhom
        return "Hello, $whom!"
    }

    @GetMapping("/hello/{who}")
    fun helloOne(
        @PathVariable("who") who: String,
    ): String {
        if (testConfig.names.contains(who)) {
            return "Correct!"
        } else {
            return "Wrong"
        }
    }

    @GetMapping("/sum")
    fun sum(
        @RequestParam("a") a: Double,
        @RequestParam("b") b: Double,
        @RequestParam("c") c: Double,
    ): String {
        if (!sumProperties.allowNegative && (a < 0 || b < 0 || c < 0)) {
            return sumProperties.errorMessage.format("You cannot sum negative values.")
        }

        if (a < sumProperties.minA || a > sumProperties.maxA) {
            return sumProperties.errorMessage.format("Variable 'a' should be between ${sumProperties.minA} and ${sumProperties.maxA}")
        }
        if (b < sumProperties.minB || b > sumProperties.maxB) {
            return sumProperties.errorMessage.format("Variable 'b' should be between ${sumProperties.minB} and ${sumProperties.maxB}")
        }
        if (c < sumProperties.minC || c > sumProperties.maxC) {
            return sumProperties.errorMessage.format("Variable 'c' should be between ${sumProperties.minC} and ${sumProperties.maxC}")
        }

        val result = a + b + c
        return sumProperties.returnTemplate.format(a, b, c, result)
    }
}
