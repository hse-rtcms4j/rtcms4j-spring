package ru.enzhine.rtcms4j.spring.client.service

interface JwtTokenProvider {
    fun getToken(): String
}
