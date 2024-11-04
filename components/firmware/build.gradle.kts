plugins {
    id("com.github.davidmc24.gradle.plugin.avro")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":components:device"))
    implementation(project(":components:psk"))
    implementation(project(":components:base85"))

    implementation(libs.commonsCodec)
    implementation(libs.logging)

    implementation(project(":components:avro"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
