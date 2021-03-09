import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

// === PLUGINS =====================================================================================

plugins {
    java
    idea
    signing
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
    id("com.jfrog.artifactory") version "4.21.0"
}

// === MAIN BUILD DETAILS ==========================================================================

group = "com.norswap"
version = "1.1.0"
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

// Publication definition
publishing.publications.create<MavenPublication>("autumn") {
    from(components["java"])
    pom.withXml {
        val root = asNode()
        root.appendNode("name", project.name)
        root.appendNode("description", project.description)
        root.appendNode("url", website)
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

signing {
    // Create a 'gradle.properties' file at the root of the project, containing the next two lines,
    // replacing the values as needed:
    // signing.gnupg.keyName=<KEY_ID>
    // signing.gnupg.passphrase=<PASSWORD_WITHOUT_QUOTES>

    // You'll need to have GnuPG installed, and it should be aliased to "gpg2"
    // (homebrew on mac links it to only "gpg" by default).

    // You are forced to use the agent, because otherwise Gradle wants a private keyring, which
    // gnupg doesn't create by default since version 2.x.y. An alternative is to export the
    // private keys to a keyring file.

    useGpgCmd()
    sign(publishing.publications["autumn"])
}

// Use `gradle bintrayUpload` target to deploy to Bintray.
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
        setPublications("autumn")
    })
}

// Use `gradle artifactoryPublish` target to deploy to Artifactory.
artifactory {
    setContextUrl("https://autumn.jfrog.io/artifactory")
    publish(closureOf<PublisherConfig> {
        setContextUrl("https://autumn.jfrog.io/artifactory")
        repository(closureOf<DoubleDelegateWrapper> {
            invokeMethod("setRepoKey", "autumn")
            invokeMethod("setUsername", System.getenv("JFROG_USER"))
            invokeMethod("setPassword", System.getenv("JFROG_KEY"))
        })
        defaultsClosure = closureOf<ArtifactoryTask> {
            publications("autumn")
        }
    })
}

// Use `gradle publishAutumnPublicationToMavenCentralRepository` to deploy to Maven Central.
// Further steps are required on https://oss.sonatype.org/ to actually publish the repository.
publishing.repositories.maven { // publishing to maven central
    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    authentication {
        // Create a 'gradle.properties' file at the root of the project, containing the next two
        // lines, replacing the values as needed:
        // mavenCentralUsername=<USERNAME>
        // mavenCentralPassword=<PASSWORD>
        name = "mavenCentral"
        credentials(PasswordCredentials::class)
    }
}

// === DEPENDENCIES ================================================================================

repositories {
    mavenCentral()
    jcenter()
    maven { // artifactory
        url = uri("https://autumn.jfrog.io/artifactory/gradle")
    }
}

dependencies {
    implementation("com.norswap:utils:2.1.8")
    testImplementation("org.testng:testng:6.14.3")
    testCompileOnly("com.google.auto.value:auto-value-annotations:1.6.2")
    testAnnotationProcessor("com.google.auto.value:auto-value:1.6.2")
}

// =================================================================================================