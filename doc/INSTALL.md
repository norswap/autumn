# Install Autumn

The project's artifacts are hosted on [Bintray].

It's also possible to use [JitPack] as an alternative (detailed instructions not provided).

[Bintray]: https://bintray.com/norswap/maven/autumn
[JitPack]: https://jitpack.io/#norswap/autumn

## Using Gradle

With the Kotlin DSL (`build.gradle.kts`):

```kotlin
repositories {
    // ...
    maven {
        url =  uri("https://dl.bintray.com/norswap/maven")
    }
}

dependencies {
    // ...
    implementation("com.norswap:autumn:1.0.0-ALPHA")
}
```

With the Groovy DSL (`build.gradle`):

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/norswap/maven"
    }
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
      <id>norswap-maven</id>
      <url>https://dl.bintray.com/norswap/maven</url>
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