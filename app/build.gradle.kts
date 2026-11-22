plugins {
    alias(libs.plugins.android.application)
}

val appVersion = providers.gradleProperty("appVersion").orElse("1.0.0").get()
val appVersionParts = appVersion.split(".").map(String::toInt)
require(appVersionParts.size == 3 && appVersionParts.all { it in 0..999 }) {
    "appVersion must use MAJOR.MINOR.PATCH with each part between 0 and 999"
}
val computedVersionCode = appVersionParts[0] * 1_000_000 + appVersionParts[1] * 1_000 + appVersionParts[2]

android {
    namespace = "kr.stonecold.removeemergencycall"
    compileSdk = 37
    compileSdkMinor = 1
    defaultConfig {
        applicationId = "kr.stonecold.removeemergencycall"
        minSdk = 33
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val releaseStore = providers.gradleProperty("keystoreFile")
                .map(::File)
                .orElse(File(projectDir, "release-keystore.jks"))
                .get()
            storeFile = releaseStore
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        warningsAsErrors = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    compileOnly(libs.libxposed.api)
}
