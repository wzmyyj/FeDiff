// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    apply(from = "repositories.gradle.kts")
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(rootProject.extra.get("androidPlugin").toString())
        classpath(kotlin("gradle-plugin", rootProject.extra.get("kotlinVersion").toString()))
//        classpath("com.novoda:bintray-release:0.9.2")
//        classpath("com.github.panpf.bintray-publish:bintray-publish:1.0.0")
        classpath(Classpath.github_maven_plugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts.kts.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}