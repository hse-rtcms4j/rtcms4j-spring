package ru.enzhine.rtcms4j.spring.client.version

import org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component
import ru.enzhine.rtcms4j.spring.client.version.SemanticVersionResolveStrategy.Companion.VERSION_RESOLVE_STRATEGY_SEMVER_NAME
import ru.enzhine.rtcms4j.spring.client.version.exception.RemoteConfigurationVersionException
import ru.enzhine.rtcms4j.spring.client.version.props.SemanticVersionResolveProperties

@Role(ROLE_INFRASTRUCTURE)
@EnableConfigurationProperties(SemanticVersionResolveProperties::class)
@Primary
@Component(VERSION_RESOLVE_STRATEGY_SEMVER_NAME)
class SemanticVersionResolveStrategy(
    private val semanticVersionResolveProperties: SemanticVersionResolveProperties,
) : VersionResolveStrategy {
    companion object {
        const val VERSION_RESOLVE_STRATEGY_SEMVER_NAME = "semver"
    }

    private val regex = Regex(semanticVersionResolveProperties.semVerPattern)

    override fun shouldPostNewVersion(
        remoteVersion: String?,
        currentVersion: String,
    ): Boolean =
        try {
            if (remoteVersion == null) {
                true
            } else {
                val new = retrieveSemVer(currentVersion)
                val prev = retrieveSemVer(remoteVersion)

                if (new.major.toLong() > prev.major.toLong()) {
                    true
                } else if (new.minor.toLong() > prev.minor.toLong()) {
                    true
                } else if (new.fix.toLong() > prev.fix.toLong()) {
                    true
                } else {
                    false
                }
            }
        } catch (ex: Throwable) {
            throw RemoteConfigurationVersionException(
                message =
                    "Failed to resolve versions remoteVersion='$remoteVersion' " +
                        "and currentVersion='$currentVersion'.",
                parent = ex,
            )
        }

    override fun shouldApplyNewVersion(
        currentVersion: String,
        newRemoteVersion: String,
    ): Boolean =
        try {
            val new = retrieveSemVer(newRemoteVersion)
            val prev = retrieveSemVer(currentVersion)

            if (semanticVersionResolveProperties.applyDifferentMajor && new.major != prev.major) {
                true
            } else if (semanticVersionResolveProperties.applyDifferentMinor && new.minor != prev.minor) {
                true
            } else if (semanticVersionResolveProperties.applyDifferentFix && new.fix != prev.fix) {
                true
            } else {
                false
            }
        } catch (ex: Throwable) {
            throw RemoteConfigurationVersionException(
                message =
                    "Failed to resolve versions currentVersion='$currentVersion' " +
                        "and newRemoteVersion='$newRemoteVersion'.",
                parent = ex,
            )
        }

    private fun retrieveSemVer(version: String): SemVer {
        val semVersion =
            regex.matchEntire(version)
                ?: throw RuntimeException("Version '$version' is not a SemVer string.")
        val major =
            semVersion.groups["major"]?.value
                ?: throw RuntimeException("SemVer '$version' does not has major part.")
        val minor =
            semVersion.groups["minor"]?.value
                ?: throw RuntimeException("Version '$version' does not has minor part.")
        val fix =
            semVersion.groups["fix"]?.value
                ?: throw RuntimeException("Version '$version' does not has fix part.")
        val extra =
            semVersion.groups["extra"]?.value
                ?: throw RuntimeException("Version '$version' does not has extra part.")

        return SemVer(major, minor, fix, extra)
    }

    private data class SemVer(
        val major: String,
        val minor: String,
        val fix: String,
        val extra: String,
    )
}
