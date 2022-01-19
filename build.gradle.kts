val letsPlotLibraryVersion = "2.2.1"
val letsPlotKotlinApiVersion = "3.1.1"
val dataframeVersion = "0.8.0-dev-808"


plugins {
    `java-library`
    application

    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.dataframe") version "0.8.0-dev-808"
}
// Make IDE aware of the generated code:
kotlin.sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin/")

group = "com.milaboratory"
version = "std-plots"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}


dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    api("org.apache.commons:commons-math3:3.6.1")

    // plots
    implementation(kotlin("stdlib"))

    implementation("org.apache.xmlgraphics:fop-transcoder:2.6")
    implementation("org.apache.pdfbox:pdfbox:2.0.21")

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
