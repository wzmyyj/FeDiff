import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Created on 2020/10/10.
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */

/**
 * Get versionCode By versionName.
 *
 * @param versionName versionName
 */
fun getVersionCodeByName(versionName: String?): Int {
    if (versionName == null) return 0
    val array = versionName.split(".").toList()
    val major = if (array.isNotEmpty()) array[0].toInt() else 0
    val minor = if (array.size > 1) array[1].toInt() else 0
    val release = if (array.size > 2) array[2].toInt() else 0
    val code = major * 1000000 + minor * 1000 + release
    println("VersionCode: $code")
    return code
}

fun DependencyHandler.implementation2(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.testImplementation2(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

fun DependencyHandler.androidTestImplementation2(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)

fun DependencyHandler.api2(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)

fun DependencyHandler.kapt2(dependencyNotation: Any): Dependency? =
    add("kapt", dependencyNotation)