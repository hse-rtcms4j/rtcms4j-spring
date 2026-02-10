package ru.enzhine.rtcms4j.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import ru.enzhine.rtcms4j.example.config.props.Anchor

@ConfigurationPropertiesScan(basePackageClasses = [Anchor::class])
@SpringBootApplication
class SpringApplication

fun main(args: Array<String>) {
    runApplication<SpringApplication>(*args)
}
