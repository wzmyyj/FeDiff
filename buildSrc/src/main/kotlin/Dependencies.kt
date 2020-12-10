/**
 * Created on 2020/09/19.
 *
 * Dependencies with Versions.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
object Dependencies {

    private object Versions {
        const val kotlin = "1.4.10"
        const val coreKtx = "1.3.0"
        const val androidx = "1.2.0"
        const val lifecycle = "2.2.0"
        const val material = "1.2.1"
    }

    // kotlin
    const val kotlin_jdk = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    // androidx
    const val androidx_coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx}"
    const val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val androidx_recyclerview = "androidx.recyclerview:recyclerview:1.1.0-beta03"
    const val androidx_lifecycle_java8 =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    const val androidx_lifecycle_ext =
        "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"

    // material
    const val material = "com.google.android.material:material:${Versions.material}"

    // test
    const val junit = "junit:junit:4.12"
    const val test_junit = "androidx.test.ext:junit:1.1.2"
    const val test_espresso = "androidx.test.espresso:espresso-core:3.2.0"

    // apt
    const val auto_service = "com.google.auto.service:auto-service:1.0-rc4"
    const val java_poet = "com.squareup:javapoet:1.10.0"

}