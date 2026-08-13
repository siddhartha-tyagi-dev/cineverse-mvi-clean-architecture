import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { inputStream ->
            load(inputStream)
        }
    }
}
val secretProperties = Properties().apply {
    val secretPropertiesFile = rootProject.file("secret.properties")
    if (secretPropertiesFile.exists()) {
        secretPropertiesFile.inputStream().use { inputStream ->
            load(inputStream)
        }
    }
}

fun Properties.token(vararg keys: String): String =
    keys.firstNotNullOfOrNull { key ->
        getProperty(key)
            ?.trim()
            ?.trim('"')
            ?.takeIf(String::isNotBlank)
    }
        .orEmpty()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

val debugTmdbAccessToken = secretProperties
    .token("dev.TMDB_ACCESS_TOKEN", "TMDB_ACCESS_TOKEN")
    .ifBlank { localProperties.token("TMDB_ACCESS_TOKEN") }

val releaseTmdbAccessToken = secretProperties
    .token("prod.TMDB_ACCESS_TOKEN", "TMDB_ACCESS_TOKEN")
    .ifBlank { localProperties.token("TMDB_ACCESS_TOKEN") }

android {
    namespace = "com.example.cineverse_mvi_clean_architecture"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.cineverse_mvi_clean_architecture"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "TMDB_ACCESS_TOKEN",
                "\"$debugTmdbAccessToken\"",
            )
        }

        release {
            buildConfigField(
                "String",
                "TMDB_ACCESS_TOKEN",
                "\"$releaseTmdbAccessToken\"",
            )

            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.dagger.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    ksp(libs.dagger.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
