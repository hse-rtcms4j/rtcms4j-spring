package ru.enzhine.rtcms4j.spring.client.version

interface VersionResolveStrategy {
    fun shouldPostNewVersion(
        remoteVersion: String?,
        currentVersion: String,
    ): Boolean

    fun shouldApplyNewVersion(
        currentVersion: String,
        newRemoteVersion: String,
    ): Boolean
}
