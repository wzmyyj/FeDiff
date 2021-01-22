plugins { id(PluginId.library) }
useKotlin()
setupCore()
android {
    buildFeatures.dataBinding = true
    defaultConfig.multiDexEnabled = true

    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = hashMapOf("DIFF_MODULE_NAME" to "login")
            }
        }
    }
}
dependencies {
    implementation(Dependencies.androidx_coreKtx)
    implementation(Dependencies.androidx_constraintlayout)
    implementation(Dependencies.androidx_recyclerview)
    implementation(Dependencies.androidx_lifecycle_ext)
    implementation(Dependencies.androidx_lifecycle_java8)

    implementation(project(":lib_diff_api"))
    kapt2(project(":lib_diff_compiler"))
}