// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springSecurityCore)

    implementation(project(":components:shared"))

    implementation(libs.commonsCodec)
    implementation(libs.logging)

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.mockk)

    testRuntimeOnly(libs.junitPlatformLauncher)
}
