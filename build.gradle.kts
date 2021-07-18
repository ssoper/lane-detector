import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.seansoper"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Temporarily disabled until new Maven package is cut
    // implementation("org.openpnp:opencv:4.5.1-2")
    implementation(files("src/main/libs/opencv-4.5.3.jar"))
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}