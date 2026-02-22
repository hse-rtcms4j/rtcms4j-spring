package ru.enzhine.rtcms4j.spring.client.service

interface FeedbackService {
    fun postFeedbackOnConfiguration(
        configurationId: Long,
        version: String,
    )

    fun postFeedbackOnSecretRotation()
}
