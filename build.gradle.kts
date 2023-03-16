plugins {
    kotlin("jvm") version "1.8.0"
    application
}

val telegramBotApiVersion : String by project
val cloudApiVersion : String by project
val googleMapsApi : String by project

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.inmo:tgbotapi:$telegramBotApiVersion")
    implementation(platform("com.google.cloud:libraries-bom:$cloudApiVersion"))
    implementation("com.google.cloud:google-cloud-translate")
    implementation("com.google.maps:google-maps-services:$googleMapsApi")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}