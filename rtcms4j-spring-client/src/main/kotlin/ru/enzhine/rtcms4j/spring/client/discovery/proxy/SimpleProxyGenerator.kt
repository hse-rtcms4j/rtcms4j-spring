package ru.enzhine.rtcms4j.spring.client.discovery.proxy

import org.springframework.aop.TargetSource

interface SimpleProxyGenerator {
    fun wrapObject(
        target: Any,
        targetSource: TargetSource,
    ): Any
}
