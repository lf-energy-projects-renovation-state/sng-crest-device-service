dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-core")

    implementation(project(":components:psk"))
    implementation(files("lib/base85.jar")) // TODO REMOVE

    implementation(libs.commonsCodec)
    implementation(libs.logging)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockitoKotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
