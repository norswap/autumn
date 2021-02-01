# Install Autumn

The project's artifacts are hosted on [Bintray] and available from JCenter.

It's also possible to use [JitPack] as an alternative (detailed instructions not provided).

[Bintray]: https://bintray.com/norswap/maven/autumn
[JitPack]: https://jitpack.io/#norswap/autumn

**Setup:** If you are using IntelliJ IDEA, make sure to define the environment variable
`AUTUMN_USE_CHAR_COLUMN` for more accurate hyperlinked file locations. The same applies if your
editor supports hyperlinked file locations with columns expressed as a character offset (tabs count
for 1) instead of width (tabs go to next multiple of the tab size).

## Using Gradle

With the Kotlin DSL (`build.gradle.kts`):

```kotlin
repositories {
    // ...
    jcenter()
}

dependencies {
    // ...
    implementation("com.norswap:autumn:1.0.0-ALPHA")
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
    implementation 'com.norswap:autumn:1.0.0-ALPHA'
}
```

## Using Maven

In `pom.xml`:

```xml
<project>
  ...
  <repositories>
    ...
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com</url>
    </repository>
  </repositories>
  <dependencies>
    ...
    <dependency>
      <groupId>com.norswap</groupId>
      <artifactId>autumn</artifactId>
      <version>1.0.0-ALPHA</version>
    </dependency>  
  </dependencies>
</project>
```