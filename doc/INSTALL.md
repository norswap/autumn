# Install Autumn

The project's artifacts are hosted on [Bintray] and available from JCenter.

It's also possible to use [JitPack] as an alternative (detailed instructions not provided).

[Bintray]: https://bintray.com/norswap/maven/autumn
[JitPack]: https://jitpack.io/#norswap/autumn

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