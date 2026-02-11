import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

apply {
    plugin("org.springframework.boot")
    plugin("org.openapi.generator")
}

val specDependency by configurations.registering {
    isCanBeConsumed = false
    isCanBeResolved = false
}
val spec by configurations.registering {
    extendsFrom(specDependency.get())
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    api("ru.enzhine:rtcms4j-core-api")
    specDependency("ru.enzhine:rtcms4j-core-api")
    api("ru.enzhine:rtcms4j-notify-api")
    specDependency("ru.enzhine:rtcms4j-notify-api")

    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-configuration-processor")
    api("org.springframework:spring-web")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("com.networknt:json-schema-validator")
    api("com.github.victools:jsonschema-generator")
    api("com.github.victools:jsonschema-module-jackson")

    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

val projectBuildDir = layout.buildDirectory.get()

tasks.register("generate-core-api", GenerateTask::class) {
    // DOCS: https://openapi-generator.tech/docs/generators/java/
    generatorName = "java"

    outputDir = "$projectBuildDir/generated"
    inputSpec.set(
        spec
            .flatMap { it.elements }
            .map { archives ->
                val archive = archives.single { it.asFile.name.contains("rtcms4j-core-api") }

                resources.text
                    .fromArchiveEntry(archive, "static/openapi/core-api.yaml")
                    .asFile()
                    .absolutePath
            },
    )
    modelPackage = "ru.enzhine.rtcms4j.core.api.dto"
    apiPackage = "ru.enzhine.rtcms4j.core.api"

    configOptions.set(
        mapOf(
            "library" to "restclient",
            "documentationProvider" to "none",
            "openApiNullable" to "false",
            "useJakartaEe" to "true",
        ),
    )
}

tasks.register("generate-notify-api", GenerateTask::class) {
    // DOCS: https://openapi-generator.tech/docs/generators/java/
    generatorName = "java"

    outputDir = "$projectBuildDir/generated"
    inputSpec.set(
        spec
            .flatMap { it.elements }
            .map { archives ->
                val archive = archives.single { it.asFile.name.contains("rtcms4j-notify-api") }

                resources.text
                    .fromArchiveEntry(archive, "static/openapi/notify-api.yaml")
                    .asFile()
                    .absolutePath
            },
    )
    modelPackage = "ru.enzhine.rtcms4j.notify.api.dto"
    apiPackage = "ru.enzhine.rtcms4j.notify.api"

    configOptions.set(
        mapOf(
            "library" to "restclient",
            "documentationProvider" to "none",
            "openApiNullable" to "false",
            "useJakartaEe" to "true",
        ),
    )
}

tasks.runKtlintCheckOverMainSourceSet {
    enabled = false
}

tasks.compileKotlin {
    dependsOn("generate-core-api")
    dependsOn("generate-notify-api")
}

sourceSets {
    main {
        java {
            srcDir("$projectBuildDir/generated/src/main/java")
        }
    }
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }

    withType<PublishToMavenRepository> {
        enabled = true
    }

    test {
        // junit fix
        useJUnitPlatform()
        // test verbose logging
        testLogging { exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
    }
}
