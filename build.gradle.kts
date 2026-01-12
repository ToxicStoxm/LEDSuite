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
    mavenLocal()
    maven {
        url = uri("./offline-repository")
    }
}

val classgraphVersion = "4.8.184"
val jakartaVersion = "2.2.0"
val javaGiVersion = "0.12.2"
val jetbrainsAnnotationsVersion = "26.0.2-1"
val junitPlatformVersion = "6.0.0"
val junitVersion = "6.0.2"
val lombokVersion = "1.18.42"
val snakeYAMLVersion = "2.5"
val yajlVersion = "2.1.0"
val yajsiVersion = "2.3.0"

dependencies {
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    compileOnly("org.projectlombok:lombok:$lombokVersion")

    implementation("com.toxicstoxm.YAJL:YAJL:$yajlVersion")
    implementation("com.toxicstoxm.YAJSI:YAJSI:$yajsiVersion")
    implementation("io.github.classgraph:classgraph:$classgraphVersion")
    implementation("io.github.jwharm.javagi:adw:$javaGiVersion")
    implementation("jakarta.websocket:jakarta.websocket-client-api:$jakartaVersion")
    implementation("org.glassfish.tyrus:tyrus-client:$jakartaVersion")
    implementation("org.glassfish.tyrus:tyrus-container-grizzly-client:$jakartaVersion")
    implementation("org.yaml:snakeyaml:$snakeYAMLVersion")

    annotationProcessor("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testCompileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")

    testAnnotationProcessor("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
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