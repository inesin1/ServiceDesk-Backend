plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "ru.gbzlat"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
}

dependencies {
    // core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.logback.classic)

    // auth
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)

    // json
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)

    // database
    implementation(libs.ktorm.core)
    implementation(libs.ktorm.jackson)
    implementation(libs.ktorm.support.mysql)
    runtimeOnly(libs.mysql.connector)

    // telegram
    implementation(libs.telegram.bot)

    // test
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test)
}
