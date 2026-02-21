package ru.enzhine.rtcms4j.spring.client.stream.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.enzhine.rtcms4j.notify.api.dto.NotificationEventDto
import ru.enzhine.rtcms4j.spring.client.config.props.ApiProperties
import ru.enzhine.rtcms4j.spring.client.config.props.Rtcms4jProperties
import ru.enzhine.rtcms4j.spring.client.service.api.JwtTokenProvider
import ru.enzhine.rtcms4j.spring.client.stream.exception.SseException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Component
class NotificationClientImpl(
    apiProperties: ApiProperties,
    rtcms4jProperties: Rtcms4jProperties,
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
) : NotificationClient {
    private val restClient =
        RestClient
            .builder()
            .baseUrl(apiProperties.notifyBaseUrl)
            .build()

    private val nid = rtcms4jProperties.namespaceId
    private val aid = rtcms4jProperties.applicationId

    override fun subscribeOnNotificationSse(
        interrupter: (InputStream) -> Unit,
        onNotification: (NotificationEventDto) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        restClient
            .method(HttpMethod.GET)
            .uri("/namespace/$nid/application/$aid/sse-stream")
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .header("Connection", "keep-alive")
            .header("Authorization", "Bearer ${jwtTokenProvider.getToken()}")
            .exchange { request, response ->
                processResponse(
                    response,
                    interrupter,
                    onNotification,
                    onError,
                )
            }
    }

    private fun processResponse(
        response: ClientHttpResponse,
        interrupter: (InputStream) -> Unit,
        onNotification: (NotificationEventDto) -> Unit,
        onError: (Throwable) -> Unit,
    ) = try {
        val statusCode = response.statusCode
        if (!statusCode.is2xxSuccessful) {
            throw SseException.ExecutionFailed(
                message = "SSE subscription failed, due to $statusCode status code.",
                statusCode = statusCode,
            )
        }

        val inputStream = response.body
        interrupter(inputStream)
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .use { reader ->
                var line: String

                try {
                    while ((reader.readLine().also { line = it }) != null) {
                        if (line.isNotEmpty()) {
                            parseNotification(line)
                                ?.let { onNotification(it) }
                        }
                    }
                } catch (ex: Throwable) {
                    throw SseException.Interrupted(
                        message = "SSE execution interrupted.",
                        parent = ex,
                    )
                }
            }
    } catch (ex: Throwable) {
        onError(ex)
    }

    private fun parseNotification(sseMessage: String): NotificationEventDto? {
        val knownPrefix = "data:"
        if (sseMessage.startsWith(knownPrefix)) {
            val content = sseMessage.substring(knownPrefix.length)

            if (content == "heartbeat") {
                return null
            }

            return objectMapper.readValue(content, NotificationEventDto::class.java)
        } else {
            throw RuntimeException("Unknown message format: $sseMessage")
        }
    }
}
