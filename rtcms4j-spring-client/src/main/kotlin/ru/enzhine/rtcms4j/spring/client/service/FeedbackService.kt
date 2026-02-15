package ru.enzhine.rtcms4j.spring.client.service

interface FeedbackService {
    fun postFeedbackOnConfiguration(
        configId: Long,
        version: String,
    )

    fun postFeedbackOnSecretRotation()
}
