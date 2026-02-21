package ru.enzhine.rtcms4j.spring.client.stream.api

import ru.enzhine.rtcms4j.notify.api.dto.NotificationEventDto
import java.io.InputStream

interface NotificationClient {
    fun subscribeOnNotificationSse(
        interrupter: (InputStream) -> Unit,
        onNotification: (NotificationEventDto) -> Unit,
        onError: (Throwable) -> Unit,
    )
}
