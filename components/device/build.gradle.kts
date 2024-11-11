// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(libs.springBootStarterDataJpa)

    implementation(project(":components:shared"))

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.mockk)
}
