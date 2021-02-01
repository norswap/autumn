import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig

// === PLUGINS =====================================================================================

plugins {
    java
    idea
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
}

// === MAIN BUILD DETAILS ==========================================================================

group = "com.norswap"
version = "1.0.5"
description = "A parser combinator library"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

val website = "https://github.com/norswap/${project.name}"
val vcs = "https://github.com/norswap/${project.name}.git"

val generatedTestDir = "build/generated/sources/annotationProcessor/java/test"

sourceSets.main.get().java.srcDirs("src")
sourceSets.test.get().java.srcDirs("test", "examples", generatedTestDir)

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test.get().useTestNG()

tasks.javadoc.get().options {
    // https://github.com/gradle/gradle/issues/7038
    this as StandardJavadocDocletOptions
    addStringOption("Xdoclint:none", "-quiet")
    if (JavaVersion.current().isJava9Compatible)
        addBooleanOption("html5", true) // nice future proofing
}

// === IDE =========================================================================================

idea.module {
    // Download javadoc & sources for dependencies.
    isDownloadJavadoc = true
    isDownloadSources = true
}

// === PUBLISHING ==================================================================================

// For Bintray: (publish with `gradle bintrayUpload`)

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    publish = true
    override = true // enables overriding versions
    pkg(closureOf<PackageConfig> {
        repo = "maven"
        name = project.name
        vcsUrl = vcs
        desc = project.description
        // https://youtrack.jetbrains.com/issue/KT-33879
        setLicenses("BSD 3-Clause")
        setPublications("bintray")
    })
}

publishing.publications.create<MavenPublication>("bintray") {
    from(components["java"])
    pom.withXml {
        val root = asNode()
        root.appendNode("description", project.description)
        root.appendNode("name", project.name)
        root.appendNode("scm").apply {
            appendNode("url", website)
            val connection = "scm:git:git@github.com:norswap/${project.name}.git"
            appendNode("connection", connection)
            appendNode("developerConnection", connection)
        }
        root.appendNode("licenses").appendNode("license").apply {
            appendNode("name", "The BSD 3-Clause License")
            appendNode("url", "$website/blob/master/LICENSE")
        }
        root.appendNode("developers").appendNode("developer").apply {
            appendNode("id", "norswap")
            appendNode("name", "Nicolas Laurent")
        }
    }
}

// === DEPENDENCIES ================================================================================

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.norswap:utils:2.1.2")
    testImplementation("org.testng:testng:6.14.3")
    testCompileOnly("com.google.auto.value:auto-value-annotations:1.6.2")
    testAnnotationProcessor("com.google.auto.value:auto-value:1.6.2")
}

// =================================================================================================