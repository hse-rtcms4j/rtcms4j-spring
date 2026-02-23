# RTCMS4J-spring client

Library for integration with [RTCMS4J](https://github.com/hse-rtcms4j/).

## integrating Your Spring Boot Project

### Adding Dependency

To integrate RTCMS4J into your Spring Boot microservice application, you need to add the `rtcms4j-spring-client-starter`
dependency to your `pom.xml` or `build.gradle` file.

The current relevant version is `0.1.3`.

### Adding Application Properties

Once the dependency is added, configure your `application.yml`, adding required properties.

Here is an example for standalone localhost RTCMS4J system deployment:

```yaml
spring:
  rtcms4j:
    namespace-id: { your namespace id }
    application-id: { your application id }
    api:
      core-base-url: http://localhost:8000/core/api/v1
      notify-base-url: http://localhost:8000/notify/api/v1
    keycloak:
      server-url: http://localhost:8080
      client-id: { your application client id }
      client-secret: { your application secret id }
```

### Adding Remote Configurations

Once you are done configuring dependencies and properties. You can add as much remote configurations as you want.

Remote configuration is a data-type (DTO, POJO, etc.) bean, that gets instantiated with default values and
later controlled by library, providing relevant values.

This library does not instantiate the data-type beans. You have to do it using Spring tools. For example,
you can use Spring `@Component` annotation for code-hardcoded default values or `@ConfigurationProperties` for
properties-first default values.

Once your bean can be instantiated you have to add only one library annotation `@RemoteConfiguration` and set version
field.
Example:

```kotlin
@RemoteConfiguration(version = "1.0.0")
@Component
class FeatureConfig(
    @field:JsonPropertyDescription("Whom to greet, when accessing page.")
    val helloWhom: String = "World",
    @field:JsonPropertyDescription("Enable greeting message.")
    val featureEnabled: Boolean = true,
)
```

You can also use Jackson `@JsonPropertyDescription` annotation to add comments for fields.

### Example

You can check out example project `rtcms4j-spring-example` in this repository. 

## Details

### @RemoteConfiguration features

Remote configuration version must be provided.
By default, class name is used to identify your remote configuration DTO.

| Field                    | Description                                                                                            | Default  |
|--------------------------|--------------------------------------------------------------------------------------------------------|----------|
| `version`                | Current application remote configuration version. Must be set.                                         |          |
| `aliasName`              | However you can override it with alias.                                                                |          |
| `remoteId`               | In case you do not want to identify your remote configuration DTO by name you can provide direct id.   |          |
| `versionResolveStrategy` | Configuration version resolve strategy for `version` field. Can be set any context-available strategy. | `semver` |

It is recommended to define MUTABLE (provide setters for properties) DTO classes for remote configuration.
Such way new values will be filled directly, enabling best performance.

However, IMMUTABLE (constructor-only properties) DTO classes are also supported. Such classes must not be final or contain final getters.
This library will create proxy for such beans.

### @RemoteConfiguration limitations

Only plain DTO classes with primitive types are supported. Such types are: numeric, integers, string, boolean, and enums, or arrays of them.

### Configuration relevance maintain strategies

Current library supports 3 configuration relevance maintain strategies (`spring.rtcms4j.maintain.type` property):

- `once` singular synchronization on startup.
- `scheduled` singular synchronization on startup with following by configured cron. Cron must be configured via
  properties.
- `stream` singular synchronization on startup with following by stream (real-time). Connection retries might be
  configured via properties.

### Configuration version resolve strategies

Current library supports 1 built-in configuration version resolve strategy (`@RemoteConfiguration` annotation
`versionResolveStrategy` field):

- `semver` semantic versioning, that creates newer versions and applies changed version (minor and fix). Resolving and
  behavior might be configured via properties.

However, user might add their own version resolve strategy by implementing
`ru.enzhine.rtcms4j.spring.client.version.VersionResolveStrategy` interface and its methods.

### Required Properties

| Property                                | Description                                      |
|-----------------------------------------|--------------------------------------------------|
| `spring.rtcms4j.namespace-id`           | Corresponding project namespace ID.              |
| `spring.rtcms4j.application-id`         | Corresponding project application ID.            |
| `spring.rtcms4j.api.core-base-url`      | RTCMS4J gateway address with core API prefix.    |
| `spring.rtcms4j.api.notify-base-url`    | RTCMS4J gateway address with notify API prefix.  |
| `spring.rtcms4j.keycloak.server-url`    | RTCMS4J Keycloak server URL.                     |
| `spring.rtcms4j.keycloak.client-id`     | Corresponding project application client ID.     |
| `spring.rtcms4j.keycloak.client-secret` | Corresponding project application client secret. |

### Additional Properties

| Property                                                            | Description                                                                                       | Default                                                              |
|---------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| `spring.rtcms4j.enabled`                                            | Whether to enable RTCMS4J integration.                                                            | `true`                                                               |
| `spring.rtcms4j.token-refresh-offset`                               | Token refresh in-advance duration.                                                                | `30s`                                                                |
| `spring.rtcms4j.client-name`                                        | Application client name to present in feedback data.                                              | Random UUID                                                          |
| `spring.rtcms4j.page-size`                                          | Fetching backend configurations batch size.                                                       | `20`                                                                 |
| `spring.rtcms4j.keycloak.realm`                                     | Keycloak realm used by RTCMS4J.                                                                   | `rtcms4j`                                                            |
| `spring.rtcms4j.maintain.type`                                      | Configuration relevance maintain strategy.                                                        | `stream`                                                             |
| `spring.rtcms4j.maintain.scheduled.cron`                            | Spring cron expression for scheduling synchronization. Required for `scheduled` maintain strategy |
| `spring.rtcms4j.maintain.stream.normal-threshold`                   | Threshold for normal stream retries.                                                              | `10`                                                                 |
| `spring.rtcms4j.maintain.stream.normal-window-seconds`              | Window duration for normal stream retries (in seconds).                                           | `10`                                                                 |
| `spring.rtcms4j.maintain.stream.normal-backoff-base-ms`             | Base backoff time for normal stream retries (in ms).                                              | `1000`                                                               |
| `spring.rtcms4j.maintain.stream.throttled-threshold`                | Threshold for throttled stream retries.                                                           | `5`                                                                  |
| `spring.rtcms4j.maintain.stream.throttled-window-seconds`           | Window duration for throttled stream retries (in seconds).                                        | `60`                                                                 |
| `spring.rtcms4j.maintain.stream.throttled-backoff-base-ms`          | Base backoff time for throttled stream retries (in ms).                                           | `5000`                                                               |
| `spring.rtcms4j.maintain.stream.max-backoff-ms`                     | Maximum backoff time for retries (in ms).                                                         | `300000`                                                             |
| `spring.rtcms4j.maintain.stream.min-backoff-ms`                     | Minimum backoff time for retries (in ms).                                                         | `100`                                                                |
| `spring.rtcms4j.features.ignore-configuration-on-match-failure`     | Whether to ignore configuration on match failure.                                                 | `false`                                                              |
| `spring.rtcms4j.features.ignore-configuration-on-creation-failure`  | Whether to ignore configuration on creation failure.                                              | `false`                                                              |
| `spring.rtcms4j.features.skip-configuration-on-remote-lost-failure` | Whether to skip configuration on remote lost failure.                                             | `true`                                                               |
| `spring.rtcms4j.features.skip-configuration-on-version-failure`     | Whether to skip configuration on version failure.                                                 | `true`                                                               |
| `spring.rtcms4j.features.skip-configuration-on-commit-failure`      | Whether to skip configuration on commit failure.                                                  | `true`                                                               |
| `spring.rtcms4j.features.skip-configuration-on-fetch-failure`       | Whether to skip configuration on fetch failure.                                                   | `true`                                                               |
| `spring.rtcms4j.version.semver.apply-different-major`               | Whether to apply different major version.                                                         | `false`                                                              |
| `spring.rtcms4j.version.semver.apply-different-minor`               | Whether to apply different minor version.                                                         | `true`                                                               |
| `spring.rtcms4j.version.semver.apply-different-fix`                 | Whether to apply different fix version.                                                           | `true`                                                               |
| `spring.rtcms4j.version.semver.semver-pattern`                      | Pattern used for semantic versioning.                                                             | `^(?<major>[0-9]+)\\.(?<minor>[0-9]+)\\.(?<fix>[0-9]+)(?<extra>.*)$` |
