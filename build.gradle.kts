import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("application")
    id("org.beryx.jlink") version "3.1.1"
    id("io.github.jwharm.flatpak-gradle-generator") version "1.5.0"
    id("io.github.crimix.replace-placeholders") version "2.0"
}

if ("@ID@".endsWith("@")) {
    group = "com.toxicstoxm.LEDSuite"
    version = "1.0.0-rc3"
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
    implementation("io.github.jwharm.javagi:adw:0.11.2")

    implementation("org.yaml:snakeyaml:2.4")

    implementation("jakarta.websocket:jakarta.websocket-client-api:2.2.0")
    implementation("org.glassfish.tyrus:tyrus-client:2.2.0")
    implementation("org.glassfish.tyrus:tyrus-container-grizzly-client:2.2.0")

    compileOnly("org.jetbrains:annotations:26.0.2")
    annotationProcessor("org.jetbrains:annotations:26.0.2")

    implementation("com.toxicstoxm.YAJSI:YAJSI:2.1.5")
    implementation("com.toxicstoxm.YAJL:YAJL:2.0.6")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("io.github.classgraph:classgraph:4.8.179")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
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

tasks.named("classes") {
    dependsOn("glibCompileResources")
}

application {
    mainClass = "com.toxicstoxm.LEDSuite.Main"
    mainModule = "com.toxicstoxm.LEDSuite"
}

tasks.named<JavaExec>("run") {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

jlink {
    addOptions(
        "--strip-debug",
        "--no-header-files",
        "--no-man-pages"
    )
    launcher {
        name = "LEDSuite"
    }
}

tasks.flatpakGradleGenerator {
    outputFile.set(
        file("flatpak-sources.json")
    )

    downloadDirectory.set(
        "./offline-repository"
    )
}

replaceSourcePlaceholders {
    enabled(true)
    filesToExpand("**/LEDSuiteApplication.java")
    extraProperties("version")
}

tasks.jar {
    dependsOn("fatJar")
    manifest {
        attributes["Main-Class"] = "com.toxicstoxm.LEDSuite.Main"
    }
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