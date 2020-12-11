plugins { id(PluginId.java_library) }

dependencies {
    fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar")))
    compileOnly(Dependencies.auto_service)
    annotationProcessor(Dependencies.auto_service)
    implementation(Dependencies.java_poet)
    api(project(":lib_diff_annotation"))
}

// encoding utf-8.
tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    source = sourceSets.main.get().java.srcDirs()
    classpath += project.files(File.pathSeparator)
    options.encoding = "UTF-8"
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

apply { plugin(PluginId.github_maven) }
group = Publish.github_group

