dependencies {
    api(project(":rtcms4j-spring-client"))
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
}
