package ru.enzhine.rtcms4j.spring.client

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [AutostartEntrypoint::class])
@ConditionalOnProperty(prefix = "spring.rtcms4j", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AutostartEntrypoint
