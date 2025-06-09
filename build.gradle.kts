import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("application")
    id("io.github.jwharm.flatpak-gradle-generator") version "1.5.0"
    id("io.github.crimix.replace-placeholders") version "3.0"
}

if ("@ID@".endsWith("@")) {
    group = "com.toxicstoxm.LEDSuite"
    version = "1.0.0-rc7"
} else {
    group = "@ID@"
    version = "@VERSION@"
}

repositories {
    mavenCentral()
    maven {
        url = uri("./offline-repository")
    }
}

dependencies {
    implementation("io.github.jwharm.javagi:adw:0.12.2")

    implementation("org.yaml:snakeyaml:2.4")

    implementation("jakarta.websocket:jakarta.websocket-client-api:2.2.0")
    implementation("org.glassfish.tyrus:tyrus-client:2.2.0")
    implementation("org.glassfish.tyrus:tyrus-container-grizzly-client:2.2.0")

    compileOnly("org.jetbrains:annotations:26.0.2")
    testCompileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")
    testAnnotationProcessor("org.jetbrains:annotations:26.0.2")

    implementation("com.toxicstoxm.YAJSI:YAJSI:2.1.5")
    implementation("com.toxicstoxm.YAJL:YAJL:2.0.6")

    compileOnly("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("io.github.classgraph:classgraph:4.8.179")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

// Replaces version placeholder, in LEDSuiteApplication with the actual version
replaceSourcePlaceholders {
    enabled(true)
    filesToExpand("**/LEDSuiteApplication.java")
    extraProperties("version")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events = setOf(TestLogEvent.PASSED)
    }
}

tasks.register<Exec>("glibCompileResources") {
    workingDir = file("src/main/resources")
    commandLine = listOf("glib-compile-resources", "LEDSuite.gresource.xml")
}

tasks.classes {
    dependsOn("glibCompileResources")
}

application {
    mainClass = "com.toxicstoxm.LEDSuite.Main"
    mainModule = "com.toxicstoxm.LEDSuite"
}

tasks.named<JavaExec>("run") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.flatpakGradleGenerator {
    outputFile.set(
        file("flatpak-sources.json")
    )

    downloadDirectory.set(
        "./offline-repository"
    )
}

tasks.jar {
    dependsOn("fatJar")
    manifest {
        attributes["Main-Class"] = "com.toxicstoxm.LEDSuite.Main"
    }
}

tasks.clean {
    // Removes the .gresource file to prevent caching
    delete("src/main/resources/LEDSuite.gresource")
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = "com.toxicstoxm.LEDSuite.Main"
    }

    archiveBaseName = "${rootProject.name}-fat"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(configurations.compileClasspath.get().filter { it.isDirectory || it.isFile }.map {
        if (it.isDirectory) it else zipTree(it)
    })

    with(tasks.jar.get())
}