package ru.enzhine.rtcms4j.spring.client.annotation;

import java.lang.annotation.*;

import static ru.enzhine.rtcms4j.spring.client.version.SemanticVersionResolveStrategy.VERSION_RESOLVE_STRATEGY_SEMVER_NAME;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemoteConfiguration {
    String version();

    String aliasName() default "";

    long remoteId() default -1L;

    String versionResolveStrategy() default VERSION_RESOLVE_STRATEGY_SEMVER_NAME;
}
