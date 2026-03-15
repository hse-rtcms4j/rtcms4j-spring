package ru.enzhine.rtcms4j.example.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.enzhine.rtcms4j.example.config.props.SumProperties

@RestController
class TestController(
    private val sumProperties: SumProperties,
) {
    @GetMapping("/sum")
    fun sum(
        @RequestParam("a") a: Double,
        @RequestParam("b") b: Double,
        @RequestParam("c") c: Double,
        @RequestParam("d") d: Double,
    ): String {
        if (!sumProperties.allowNegative && (a < 0 || b < 0 || c < 0 || d < 0)) {
            return sumProperties.errorMessage.format("You cannot sum negative values.")
        }

        if (a < sumProperties.minA || a > sumProperties.maxA) {
            return sumProperties.errorMessage
                .format("Variable 'a' should be between ${sumProperties.minA} and ${sumProperties.maxA}")
        }
        if (b < sumProperties.minB || b > sumProperties.maxB) {
            return sumProperties.errorMessage
                .format("Variable 'b' should be between ${sumProperties.minB} and ${sumProperties.maxB}")
        }
        if (c < sumProperties.minC || c > sumProperties.maxC) {
            return sumProperties.errorMessage
                .format("Variable 'c' should be between ${sumProperties.minC} and ${sumProperties.maxC}")
        }
        if (d < sumProperties.minD || d > sumProperties.maxD) {
            return sumProperties.errorMessage
                .format("Variable 'd' should be between ${sumProperties.minD} and ${sumProperties.maxD}")
        }

        val result = a + b + c + d
        return sumProperties.returnTemplate.format(a, b, c, d, result)
    }

    @GetMapping("/multiply")
    fun multiply(
        @RequestParam a: Double,
        @RequestParam b: Double,
    ): Double = a * b
}
