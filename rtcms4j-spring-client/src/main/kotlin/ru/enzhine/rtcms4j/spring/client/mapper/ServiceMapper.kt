package ru.enzhine.rtcms4j.spring.client.mapper

import ru.enzhine.rtcms4j.core.api.dto.ConfigurationDetailedDto
import ru.enzhine.rtcms4j.spring.client.service.dto.BackendState

fun ConfigurationDetailedDto.toService(): BackendState? =
    if (commitVersion == null || jsonValues == null) {
        null
    } else {
        BackendState(
            version = commitVersion,
            jsonValues = jsonValues,
        )
    }
