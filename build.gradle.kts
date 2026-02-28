import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("application")
    alias(libs.plugins.flatpak.generator)
    alias(libs.plugins.replace.placeholders)
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
    mavenLocal()
    maven {
        url = uri("./offline-repository")
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)

    implementation(libs.yajl)
    implementation(libs.yajsi)
    implementation(libs.classgraph)
    implementation(libs.javagi.adw)
    implementation(libs.jakarta.websocket.client)
    implementation(libs.tyrus.client)
    implementation(libs.tyrus.grizzly.client)

    annotationProcessor(libs.jetbrains.annotations)
    annotationProcessor(libs.lombok)

    testCompileOnly(libs.jetbrains.annotations)
    testCompileOnly(libs.lombok)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testAnnotationProcessor(libs.jetbrains.annotations)
    testAnnotationProcessor(libs.lombok)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
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
    description = "Compiles the glib resource file (.gresource.xml)."
    group = JavaBasePlugin.BUILD_TASK_NAME
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
    description = "Creates a so called 'fatJar' including all of the applications dependencies."
    group = JavaBasePlugin.BUILD_TASK_NAME
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