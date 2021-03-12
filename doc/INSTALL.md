# Install Autumn

The project's artifacts are hosted on [Maven Central], and on a public [Artifactory repository].

The only difference is that new releases will land on the artifactory repository a few hours
earlier.

[Maven Central]: https://search.maven.org/artifact/com.norswap/autumn/
[Artifactory repository]: https://norswap.jfrog.io/artifactory/maven/

**Setup:** If you are using IntelliJ IDEA, make sure to define the environment variable
`AUTUMN_USE_CHAR_COLUMN` for more accurate hyperlinked file locations. The same applies if your
editor supports hyperlinked file locations with columns expressed as a character offset (tabs count
for 1) instead of width (tabs go to next multiple of the tab size).

**Version:** If the version in this file is not current, don't forget to replace it by a recent
version! Aim to use a version listed on the [Github releases page][releases].

[releases]: https://github.com/norswap/autumn/releases/

## Using Gradle

With the Kotlin DSL (`build.gradle.kts`):

```kotlin
repositories {
    // ...
    jcenter()
}

dependencies {
    // ...
    implementation("com.norswap:autumn:1.2.0")
}
```

With the Groovy DSL (`build.gradle`):

```groovy
repositories {
    // ...
    jcenter()
}

dependencies {
    // ...
    implementation 'com.norswap:autumn:1.2.0'
}
```

## Using Maven

In `pom.xml`:

```xml
<project>
  ...
  <repositories>
    ...
    <!-- no repository declaration needed for using Maven Central -->
    <repository>
        <id>artifactory-norswap</id>
        <url>https://norswap.jfrog.io/artifactory/maven</url>
    </repository>
  </repositories>
  <dependencies>
    ...
    <dependency>
      <groupId>com.norswap</groupId>
      <artifactId>autumn</artifactId>
      <version>1.2.0</version>
    </dependency>  
  </dependencies>
</project>
```