import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "io.github.szpontium"
version = "0.2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

sourceSets {
    main {
        kotlin.srcDir("../composeApp/src/commonMain/kotlin/io/github/szpontium/api")
        kotlin.exclude("**/LibrusMapper.kt")
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.whyoleg.crypto.core)
    implementation(libs.whyoleg.crypto.jdk)
    implementation(libs.ksoup)
    implementation(libs.urlencoder)
    runtimeOnly(libs.slf4j.nop)
    testImplementation(kotlin("test"))
}

application {
    applicationName = "dzienniczek"
    mainClass = "io.github.szpontium.cli.MainKt"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

distributions {
    main {
        contents {
            from("../completions") { into("completions") }
            from("../docs/cli.md") { into("docs") }
            from("../LICENSE")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Tar>().configureEach {
    compression = Compression.GZIP
    archiveExtension = "tar.gz"
}
