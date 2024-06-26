import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val protobufVersion by extra("3.21.7")
val protobufPluginVersion by extra("0.8.14")
val grpcVersion by extra("1.40.1")

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.9.22"
    id("com.google.protobuf") version "0.8.17"
}



group = "com.trb_client"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.1.0:jdk7@jar"
        }
    }
//
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.apache.kafka:kafka-clients")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:1.40.1")
    implementation("io.grpc:grpc-kotlin-stub:1.1.0")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("net.devh:grpc-spring-boot-starter:2.14.0.RELEASE")

    implementation("net.devh:grpc-client-spring-boot-starter:2.12.0.RELEASE")
    compileOnly("jakarta.annotation:jakarta.annotation-api:1.3.5")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.retry:spring-retry:2.0.3")
    implementation("org.springframework:spring-aspects:6.0.11")


    implementation("com.google.firebase:firebase-admin:8.0.0")
    implementation("io.github.resilience4j:resilience4j-reactor:1.7.1")
    implementation("io.github.resilience4j:resilience4j-spring-boot2:1.7.1")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:1.7.1")


    implementation("net.logstash.logback:logstash-logback-encoder:6.6")
    implementation("org.codehaus.janino:janino")


    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin")
    implementation("net.ttddyy.observation:datasource-micrometer-spring-boot:1.0.3")



}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.wrapper {
    gradleVersion = "7.6"
    // You can either download the binary-only version of Gradle (BIN) or
    // the full version (with sources and documentation) of Gradle (ALL)
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<Test> {
    useJUnitPlatform()
}
