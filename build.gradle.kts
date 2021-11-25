plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.21"
    id("jacoco")
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21")
    implementation("org.jetbrains.kotlinx:lincheck:2.14.1")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
    maxHeapSize = "2G"
    jvmArgs(
        "-Xmx4096m",
        "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-exports", "java.base/jdk.internal.util=ALL-UNNAMED"
    )
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(false)
        csv.required.set(true)
        csv.outputLocation.set(file("${buildDir}/jacoco/report.csv"))
        html.outputLocation.set(file("${buildDir}/reports/jacoco"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
