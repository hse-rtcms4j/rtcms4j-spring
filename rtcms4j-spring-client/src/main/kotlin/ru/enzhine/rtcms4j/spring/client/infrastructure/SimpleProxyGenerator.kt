package ru.enzhine.rtcms4j.spring.client.infrastructure

import org.springframework.aop.TargetSource

interface SimpleProxyGenerator {
    fun wrapObject(
        target: Any,
        targetSource: TargetSource,
    ): Any
}
