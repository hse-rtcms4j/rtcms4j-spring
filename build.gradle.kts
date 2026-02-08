import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

fun RepositoryHandler.github(repo: String) = maven {
    name = "GitHubPackages"
    url = URI.create("https://maven.pkg.github.com/$repo")
    credentials {
        // picks from: .../user/.gradle/gradle.properties
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("GITHUB_LOGIN") as String?
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("GITHUB_TOKEN") as String?
    }
}

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    id("org.springframework.boot") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("org.openapi.generator") apply false
    id("io.spring.dependency-management")
    id("maven-publish")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("io.spring.dependency-management")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.openapi.generator")
        plugin("org.springframework.boot")
        plugin("maven-publish")
    }

    val groupId: String by project
    val versionIdNumber: String by project
    val versionIdStatus: String by project

    group = groupId
    val versionId: String = if (versionIdStatus.isEmpty()) versionIdNumber else "$versionIdNumber-$versionIdStatus"
    version = versionId

    dependencyManagement {
        imports {
            val springBootVersion: String by project
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }

        dependencies {
            val jsonSchemaValidatorVersion: String by project
            dependency("com.networknt:json-schema-validator:$jsonSchemaValidatorVersion")

            val cucumberVersion: String by project
            dependency("io.cucumber:cucumber-jvm:$cucumberVersion")
            dependency("io.cucumber:cucumber-spring:$cucumberVersion")
            dependency("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

            val mockitoKotlin: String by project
            dependency("org.mockito.kotlin:mockito-kotlin:$mockitoKotlin")

            val rtcms4jCore: String by project
            dependency("ru.enzhine:rtcms4j-core-api:$rtcms4jCore")

            val rtcms4jNotify: String by project
            dependency("ru.enzhine:rtcms4j-notify-api:$rtcms4jNotify")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                this.groupId = groupId
                this.artifactId = project.name
                this.version = versionId
                from(components["java"])
            }
        }
        repositories {
            github("hse-rtcms4j/rtcms4j-spring")
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
