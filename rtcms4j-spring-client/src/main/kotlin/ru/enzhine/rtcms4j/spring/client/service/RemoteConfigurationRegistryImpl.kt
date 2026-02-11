package ru.enzhine.rtcms4j.spring.client.service

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.infrastructure.exception.RemoteConfigurationBeanRegistrationException
import ru.enzhine.rtcms4j.spring.client.service.dto.RemoteConfigurationEntry

@Role(ROLE_INFRASTRUCTURE)
@Component
class RemoteConfigurationRegistryImpl : RemoteConfigurationRegistry {
    private val beanEntries = mutableSetOf<RemoteConfigurationEntry>()

    override fun register(entry: RemoteConfigurationEntry) {
        val existing = beanEntries.find { it.configName == entry.configName }
        if (existing != null) {
            throw RemoteConfigurationBeanRegistrationException(
                message =
                    "RemoteConfiguration bean ${existing.beanName} ${existing.beanClass} " +
                        "named ${existing.configName} already registered.",
                parent = null,
            )
        }

        beanEntries.add(entry)
    }

    override fun entries(): List<RemoteConfigurationEntry> = beanEntries.toList()
}
