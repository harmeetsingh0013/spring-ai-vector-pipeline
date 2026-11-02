plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "in.harmeetsingh"
version = "0.0.1-SNAPSHOT"
description = "sprint-ai-vector-pipeline"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "2.0.1"
extra["springCloudVersion"] = "2025.1.4"
extra["springFunctionsCatalogVersion"] = "6.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-qdrant")
    implementation("org.springframework.ai:spring-ai-tika-document-reader")
    implementation("org.springframework.ai:spring-ai-vector-store-advisor")
    implementation("org.springframework.cloud:spring-cloud-function-web")
    implementation("org.springframework.cloud.fn:spring-file-supplier")
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.cloud.fn:spring-functions-catalog-bom:${property("springFunctionsCatalogVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
