package ru.enzhine.rtcms4j.example.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import ru.enzhine.rtcms4j.example.config.props.FeatureConfig
import ru.enzhine.rtcms4j.example.config.props.TestConfig

@RestController
class TestController(
    private val featureConfig: FeatureConfig,
    private val testConfig: TestConfig,
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
}
