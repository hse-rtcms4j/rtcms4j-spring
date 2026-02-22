import java.time.LocalDateTime

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
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

val groupId: String by rootProject

val versionIdNumber: String by rootProject
val versionIdStatus: String by rootProject
val versionId: String = if (versionIdStatus.isEmpty()) versionIdNumber else "$versionIdNumber-$versionIdStatus"

mavenPublishing {
    val rootName = rootProject.name
    val projectName = project.name
    coordinates(groupId, projectName, versionId)

    pom {
        name.set(projectName)
        description.set(rootProject.description)
        inceptionYear.set(LocalDateTime.now().year.toString())
        url.set("https://github.com/hse-rtcms4j/$rootName")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Enzhine")
                name.set("Onar")
                url.set("https://github.com/enzhine/")
            }
        }
        scm {
            url.set("https://github.com/hse-rtcms4j/$rootName")
            connection.set("scm:git:git://github.com/hse-rtcms4j/$rootName.git")
            developerConnection.set("scm:git:ssh://git@github.com/hse-rtcms4j/$rootName.git")
        }
    }
}
