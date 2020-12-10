plugins { id(PluginId.application) }
useKotlin()
setupCore()
android {
    buildFeatures.dataBinding = true
    defaultConfig.multiDexEnabled = true
    defaultConfig.applicationId = AppConfig.applicationId
    flavorDimensions("channel")
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