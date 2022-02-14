import com.palantir.gradle.gitversion.VersionDetails

val letsPlotLibraryVersion = "2.2.1"
val letsPlotKotlinApiVersion = "3.1.1"
val dataframeVersion = "0.8.0-dev-808"

plugins {
    `java-library`
    application
    `maven-publish`

    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.dataframe") version "0.8.0-dev-808"
    id("com.palantir.git-version") version "0.13.0"
}

// Make IDE aware of the generated code:
kotlin.sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin/")

val miRepoAccessKeyId: String by project
val miRepoSecretAccessKey: String by project

val miGitHubMavenUser: String? by project
val miGitHubMavenToken: String? by project

val productionBuild: Boolean? by project

val versionDetails: groovy.lang.Closure<VersionDetails> by extra
val gitDetails = versionDetails()

fun boolProperty(name: String): Boolean {
    return ((properties[name] as String?) ?: "false").toBoolean()
}

val isMiCi: Boolean = boolProperty("mi-ci")
val isRelease: Boolean = boolProperty("mi-release")

val longTests: String? by project
val miCiStage: String? = properties["mi-ci-stage"] as String?

group = "com.milaboratory"
val gitLastTag = gitDetails.lastTag.removePrefix("v")

version = if (version != "unspecified") {
    version
} else if (gitDetails.commitDistance == 0) {
    gitLastTag
} else {
    "${gitLastTag}-${gitDetails.commitDistance}-${gitDetails.gitHash}"
}
description = "MiPlots"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    api("org.apache.commons:commons-math3:3.6.1")

    // plots
    implementation(kotlin("stdlib"))

    implementation("org.apache.xmlgraphics:fop-transcoder:2.6")
    implementation("org.apache.pdfbox:pdfbox:2.0.24")

    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("org.jetbrains.kotlinx:dataframe:$dataframeVersion")
    implementation("org.jetbrains.lets-plot:lets-plot-common:$letsPlotLibraryVersion")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:$letsPlotKotlinApiVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

publishing {
    repositories {
        if (miGitHubMavenUser != null && miGitHubMavenUser != "") {
            maven {
                name = "GitHub"
                url = uri("https://maven.pkg.github.com/milaboratory/miplots")

                credentials {
                    username = miGitHubMavenUser
                    password = miGitHubMavenToken
                }
            }
        }
    }
}
