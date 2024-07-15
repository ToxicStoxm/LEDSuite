@file:Suppress("UnstableApiUsage")

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.org.fusesource.jansi.jansi)
    implementation(libs.io.github.jwharm.javagi.adw)
    implementation(libs.org.apache.commons.commons.configuration2)
    implementation(libs.org.yaml.snakeyaml)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)
}

group = "com.toxicstoxm.lccp"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_22

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.toxicstoxm.lccp.LCCP"
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

// Shadow plugin configuration
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {

    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.toxicstoxm.lccp.LCCP"
    }
    archiveFileName.set("LED-Cube-Control-Panel-$version.jar")
    mergeServiceFiles()
    configurations = listOf(project.configurations.runtimeClasspath.get())
}

tasks.register<Copy>("processVersionProperties") {
    from("src/main/resources/version.properties")
    into(layout.buildDirectory.dir("/generated/resources"))
    filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf("version" to version))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("processVersionProperties")
    from(layout.buildDirectory.dir("/generated/resources")) {
        include("version.properties")
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.build {
    dependsOn(tasks.processResources)
    dependsOn(tasks.named("shadowJar"))
}

