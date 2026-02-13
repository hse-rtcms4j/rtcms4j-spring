package ru.enzhine.rtcms4j.example.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.enzhine.rtcms4j.example.config.props.FeatureConfig

@RestController
class TestController(
    private val featureConfig: FeatureConfig,
) {
    @GetMapping("/hello")
    fun hello(): String {
        if (Math.random() > featureConfig.secretChance) {
            return "I always come back!"
        }
        val whom = featureConfig.helloWhom
        return "Hello, $whom!"
    }
}
