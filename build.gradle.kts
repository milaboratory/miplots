import com.palantir.gradle.gitversion.VersionDetails
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.InetAddress

plugins {
    `java-library`
    application
    `maven-publish`

    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.dataframe") version "0.8.0-rc-7"
    id("com.palantir.git-version") version "0.13.0"
}

// Make IDE aware of the generated code:
kotlin.sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin/")

val miRepoAccessKeyId: String? by project
val miRepoSecretAccessKey: String? by project

val versionDetails: groovy.lang.Closure<VersionDetails> by extra
val gitDetails = versionDetails()

fun boolProperty(name: String): Boolean {
    return ((properties[name] as String?) ?: "false").toBoolean()
}

// val isMiCi: Boolean = boolProperty("mi-ci")
// val isRelease: Boolean = boolProperty("mi-release")

group = "com.milaboratory"
version = if (version != "unspecified") version else ""
description = "MiPlots"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.withType<KotlinCompile> { // this affects to all kotlinCompilation tasks
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
}

val letsPlotLibraryVersion = "2.2.1"
val letsPlotKotlinApiVersion = "3.1.1"
val dataframeVersion = "0.8.0-rc-7"

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    api("org.apache.commons:commons-math3:3.6.1")

    // plots
    implementation("org.apache.xmlgraphics:fop-transcoder:2.6")
    implementation("org.apache.pdfbox:pdfbox:2.0.24")
    implementation("org.apache.commons:commons-csv:1.9.0")
    api("org.jetbrains.kotlinx:dataframe:$dataframeVersion")
    api("org.jetbrains.lets-plot:lets-plot-common:$letsPlotLibraryVersion")
    api("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:$letsPlotKotlinApiVersion")
}

val writeBuildProperties by tasks.registering(WriteProperties::class) {
    outputFile = file("${sourceSets.main.get().output.resourcesDir}/${project.name}-build.properties")
    property("version", version)
    property("name", description)
    property("revision", gitDetails.gitHash)
    property("branch", gitDetails.branchName ?: "no_branch")
    property("host", InetAddress.getLocalHost().hostName)
    property("timestamp", System.currentTimeMillis())
}

val createScratch by tasks.registering {
    doLast {
        mkdir("scratch")
    }
}

tasks.test {
    dependsOn(createScratch)
    useJUnitPlatform()
}

publishing {
    repositories {
        if (miRepoAccessKeyId != null) {
            maven {
                name = "mipub"
                url = uri("s3://milaboratory-artefacts-public-files.s3.eu-central-1.amazonaws.com/maven")

                authentication {
                    credentials(AwsCredentials::class) {
                        accessKey = miRepoAccessKeyId!!
                        secretKey = miRepoSecretAccessKey!!
                    }
                }
            }
        }
    }
}
