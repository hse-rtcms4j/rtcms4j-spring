package ru.enzhine.rtcms4j.spring.client.version

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.version.SemanticVersionResolveStrategy.Companion.VERSION_RESOLVE_STRATEGY_SEM_VER_NAME
import ru.enzhine.rtcms4j.spring.client.version.props.SemanticVersionResolveProperties

@Role(ROLE_INFRASTRUCTURE)
@EnableConfigurationProperties(SemanticVersionResolveProperties::class)
@Component(VERSION_RESOLVE_STRATEGY_SEM_VER_NAME)
class SemanticVersionResolveStrategy(
    private val semanticVersionResolveProperties: SemanticVersionResolveProperties,
) : VersionResolveStrategy {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        const val VERSION_RESOLVE_STRATEGY_SEM_VER_NAME = "SemVer"
    }

    private val regex = Regex(semanticVersionResolveProperties.semVerPattern)

    override fun shouldApplyNewVersion(
        previousVersion: String,
        newVersion: String,
    ): Boolean =
        try {
            val new = retrieveSemVer(newVersion)
            val prev = retrieveSemVer(previousVersion)

            if (semanticVersionResolveProperties.applyDifferentMajor && new.major != prev.major) {
                true
            } else if (semanticVersionResolveProperties.applyDifferentMinor && new.minor != prev.minor) {
                true
            } else if (semanticVersionResolveProperties.applyDifferentFix && new.fix != prev.fix) {
                true
            } else {
                false
            }
        } catch (ex: RuntimeException) {
            logger.error("Ignoring version '$newVersion'.", ex)
            false
        }

    private fun retrieveSemVer(version: String): SemVer {
        val semVersion =
            regex.matchEntire(version)
                ?: throw RuntimeException("Version '$version' is not a SemVer string.")
        val major =
            semVersion.groups["major"]?.value
                ?: throw RuntimeException("SemVer '$version' does not has major.")
        val minor =
            semVersion.groups["minor"]?.value
                ?: throw RuntimeException("Version '$version' does not has minor.")
        val fix =
            semVersion.groups["fix"]?.value
                ?: throw RuntimeException("Version '$version' does not has fix.")

        return SemVer(major, minor, fix)
    }

    private data class SemVer(
        val major: String,
        val minor: String,
        val fix: String,
    )
}
