package ru.enzhine.rtcms4j.spring.client.service.api

interface JwtTokenProvider {
    fun getToken(): String
}
