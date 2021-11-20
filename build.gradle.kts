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
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
    maxHeapSize = "2G"
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