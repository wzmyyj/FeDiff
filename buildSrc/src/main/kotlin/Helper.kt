import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName

/**
 * Created on 2020/10/10.
 *
 * Gradle config helper.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
private val Project.android_ get() = extensions.getByName<BaseExtension>("android")

/**
 * Do core gradle config.
 */
fun Project.setupCore() {
    android_.apply {
        compileSdkVersion(AndroidConfig.compileSdkVersion)
        buildToolsVersion(AndroidConfig.buildToolsVersion)
        defaultConfig {
            minSdkVersion(AndroidConfig.minSdkVersion)
            targetSdkVersion(AndroidConfig.targetSdkVersion)
            versionCode = getVersionCodeByName(AppConfig.versionName)
            versionName = AppConfig.versionName
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            consumerProguardFiles("consumer-rules.pro")
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    dependencies {
        fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar", "*.aar")))
        implementation2(Dependencies.androidx_appcompat)
        testImplementation2(Dependencies.junit)
        androidTestImplementation2(Dependencies.test_junit)
        androidTestImplementation2(Dependencies.test_espresso)
    }
}

/**
 * Use kotlin in this module.
 */
fun Project.useKotlin() {
    apply {
        plugin("kotlin-android")
        plugin("kotlin-android-extensions")
        plugin("kotlin-kapt")
    }
}

