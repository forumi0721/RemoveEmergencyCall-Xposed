plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "kr.stonecold.removeemergencycall"
    compileSdk = 36

    defaultConfig {
        applicationId = "kr.stonecold.removeemergencycall"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = File(projectDir, "release-keystore.jks")
            storePassword = System.getenv("storePassword")
            keyAlias = System.getenv("keyAlias")
            keyPassword = System.getenv("keyPassword")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        disable += setOf("OldTargetApi", "GradleDependency", "AndroidGradlePluginVersion")
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
