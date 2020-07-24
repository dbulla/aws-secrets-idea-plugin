buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", version = "1.3.72")) }
}

plugins {
    id("org.jetbrains.intellij") version "0.4.21"
    kotlin("jvm") version "1.3.72"
    id("se.patrikerdes.use-latest-versions") version "0.2.14"
    id("com.github.ben-manes.versions") version "0.29.0"
}

intellij {
    updateSinceUntilBuild = false
    instrumentCode = true
    version = "2020.1.4"
}

group = "com.nurflugel"
version = "0.0.1"

repositories {
    jcenter()
    mavenCentral()
}

val awsVersion = "1.11.824"

dependencies {
    implementation("com.amazonaws:aws-java-sdk-secretsmanager:$awsVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.10.0")
}
