apply {
    plugin("org.springframework.boot")
}

dependencies {
    api(project(":rtcms4j-spring-client-starter"))

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = false
    }
}
