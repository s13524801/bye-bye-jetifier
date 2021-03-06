[![Dipien](https://raw.githubusercontent.com/maxirosson/sample/master/.github/logo4.png)](http://www.dipien.com)

# Bye Bye Jetifier Gradle Plugin
Gradle Plugin to verify if you can keep Android Jetifier disabled

You can read more details about this plugin on this [article](https://medium.com/dipien/say-bye-bye-to-android-jetifier-a7e0d388f5d6).

## Features
This plugin verifies on each dependency JAR/AAR (and its transitives) if:
* any class is using a support library import
* any layout is referencing a support library class
* the Android Manifest is referencing a support library class

It also verifies if any support library dependency is resolved on the project.

#### Why should I use this plugin instead of can-i-drop-jetifier?

The [can-i-drop-jetifier](https://github.com/plnice/can-i-drop-jetifier) plugin only checks for legacy support libraries on the dependencies graph. That's not enough to decide if you can drop Jetifier. Lots of libraries don't properly declare on their POMs the legacy support libraries they use as transitive dependencies. So, for those cases, `can-i-drop-jetifier` says that you can disable Jetifier. But, if you do that, then you are going to have runtime errors when the logic using the legacy support library is executed.

`Bye bye Jetifier` inspects each JAR/AAR, searching for legacy support libraries usages, so it will find more libraries than `can-i-drop-jetifier`, and you will avoid those runtime errors.

## Setup

Add the following configuration to your root `build.gradle`, replacing X.Y.Z by the [latest version](https://github.com/dipien/bye-bye-jetifier/releases/latest)

```groovy
buildscript {
    repositories {
        mavenCentral() // or gradlePluginPortal()
    }
    dependencies {
        classpath("com.dipien:bye-bye-jetifier:X.Y.Z")
    }
}

apply plugin: "com.dipien.byebyejetifier"
```

## Usage

To validate if your project dependencies (and its transitives) have any usage of the legacy android support library, you need to execute the following task:

    ./gradlew canISayByeByeJetifier -Pandroid.enableJetifier=false

If you have any legacy android support library usage, the task will fail and print a report with all the details. For example:

```
========================================
Project: app
========================================

Scanning com.github.bumptech.glide:glide:3.8.0
 * com/bumptech/glide/Glide.class -> android/support/v4/app/FragmentActivity
 * com/bumptech/glide/Glide.class -> android/support/v4/app/Fragment
 * com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/FragmentManager
 * com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/FragmentActivity
 * com/bumptech/glide/manager/RequestManagerRetriever.class -> android/support/v4/app/Fragment
 * com/bumptech/glide/manager/SupportRequestManagerFragment.class -> android/support/v4/app/Fragment

Legacy support dependencies:
 * com.android.support:support-annotations:28.0.0

> Task :canISayByeByeJetifier FAILED
```

If you don't have any legacy android support library usages, the task will finish successfully, so it's safe to remove the `android.enableJetifier` flag from your `gradle.properties`.

Once you have disabled jetifier, you don't want to add a new support-library-dependent library by mistake when adding/upgrading a dependency on your project. To avoid that kind of issues, you can run the `canISayByeByeJetifier` task on your CI tool as part of the PR checks.

## Advanced configuration
You can configure the plugin using the `byeByeJetifier` extension. These are the default values for each property:

```groovy
byeByeJetifier {
    legacyGroupIdPrefixes = ["android.arch", "com.android.support"]
    excludedConfigurations = ["lintClassPath"]
    excludedFilesFromScanning = [
        // org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.20
        "org/jetbrains/kotlin/load/java/JvmAnnotationNamesKt",

        // org.jetbrains.kotlin:kotlin-reflect:1.4.20
        "kotlin/reflect/jvm/internal/impl/load/java/JvmAnnotationNamesKt",

        // org.jetbrains.kotlin:kotlin-android-extensions:1.4.20
        "org/jetbrains/kotlin/android/synthetic/AndroidConst",
        "org/jetbrains/kotlin/android/synthetic/codegen/AndroidIrTransformer",
        "org/jetbrains/kotlin/android/synthetic/codegen/ResourcePropertyStackValue",

        // org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.10
        "org/jetbrains/kotlin/com/intellij/codeInsight/NullableNotNullManager"
    ]
    excludeSupportAnnotations = true
    verbose = false
}
```
## Versioning

This project uses the [Semantic Versioning guidelines](http://semver.org/) for transparency into our release cycle.

## Donations

Donations are greatly appreciated. You can help us to pay for our domain and this project development.

* [Donate cryptocurrency](http://coinbase.dipien.com/)
* [Donate with PayPal](http://paypal.dipien.com/)
* [Donate on Patreon](http://patreon.dipien.com/)

## Follow us
* [Twitter](http://twitter.dipien.com)
* [Medium](http://medium.dipien.com)
* [Blog](http://blog.dipien.com)
