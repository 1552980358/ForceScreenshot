plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "projekt.cloud.piece.force.screenshot"
        minSdk = 21
        targetSdk = 31
        versionCode = 4
        versionName = "1.2.1"
    }

    buildTypes {
        @Suppress("UNUSED_VARIABLE")
        val release by getting {
            isMinifyEnabled = false
        }
        @Suppress("UNUSED_VARIABLE")
        val debug by getting {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}