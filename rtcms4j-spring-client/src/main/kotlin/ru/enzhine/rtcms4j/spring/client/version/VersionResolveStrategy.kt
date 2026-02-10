package ru.enzhine.rtcms4j.spring.client.version

interface VersionResolveStrategy {
    fun shouldApplyNewVersion(
        previousVersion: String,
        newVersion: String,
    ): Boolean
}
