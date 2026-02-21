package ru.enzhine.rtcms4j.spring.client.discovery.registry

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.discovery.exception.RemoteConfigurationBeanRegistrationException
import ru.enzhine.rtcms4j.spring.client.discovery.registry.dto.LocalConfigurationEntry

@Role(ROLE_INFRASTRUCTURE)
@Component
class LocalConfigurationRegistryImpl : LocalConfigurationRegistry {
    private val beanEntries = mutableSetOf<LocalConfigurationEntry>()

    override fun register(entry: LocalConfigurationEntry) {
        if (entry.configId != null) {
            val registeredById = beanEntries.find { it.configId == entry.configId }
            if (registeredById != null) {
                throw RemoteConfigurationBeanRegistrationException(
                    "Unable to register ${entry.beanName}," +
                        "because ${registeredById.beanName} already claimed ${registeredById.configId} id.",
                )
            }
        } else {
            val registeredByName = beanEntries.find { it.configId == null && it.configurationName == entry.configurationName }
            if (registeredByName != null) {
                throw RemoteConfigurationBeanRegistrationException(
                    "Unable to register ${entry.beanName}," +
                        "because ${registeredByName.beanName} already claimed ${registeredByName.configurationName}.",
                )
            }
        }

        beanEntries.add(entry)
    }

    override fun entries(): List<LocalConfigurationEntry> = beanEntries.toList()
}
