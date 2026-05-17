plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

import java.util.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Auto-versioning configuration - Read from version.properties
val versionPropsFile = file("../version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
}

val majorVersion = versionProps.getProperty("MAJOR_VERSION", "1").toInt()
val minorVersion = versionProps.getProperty("MINOR_VERSION", "0").toInt()
val patchVersion = versionProps.getProperty("PATCH_VERSION", "0").toInt()

// Auto-increment version code based on timestamp
// This ensures each build has a unique, incrementing version code
fun getAutoVersionCode(): Int {
    // Use days since epoch to create incrementing version code
    val daysFromEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    // Add build number from current time (ensures uniqueness even on same day)
    val buildNumber = (System.currentTimeMillis() / 1000 / 60) % 1000
    return (daysFromEpoch.toInt() * 1000 + buildNumber).toInt()
}

// Auto-generate version name with build timestamp
fun getAutoVersionName(): String {
    val buildDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    val buildTime = SimpleDateFormat("HHmm", Locale.US).format(Date())
    return "$majorVersion.$minorVersion.$patchVersion-build.$buildDate.$buildTime"
}

// Simple version name without build info (for release builds)
fun getSimpleVersionName(): String {
    return "$majorVersion.$minorVersion.$patchVersion"
}

android {
    namespace = "com.crosssafe.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.crosssafe.app"
        minSdk = 26
        targetSdk = 35

        // Automatic versioning
        versionCode = getAutoVersionCode()
        versionName = getAutoVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add version info to BuildConfig for easy access
        buildConfigField("String", "VERSION_INFO", "\"v${getAutoVersionName()} (${getAutoVersionCode()})\"")
        buildConfigField("String", "BUILD_DATE", "\"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\"")
        buildConfigField("String", "SIMPLE_VERSION", "\"${getSimpleVersionName()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug builds get detailed version with timestamp
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

android.applicationVariants.all {
    val variant = this
    variant.outputs.all {
        val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        // Custom output file name with version code
        if (variant.buildType.name == "release") {
            output.outputFileName = "app-release-${variant.versionCode}.apk"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.viewpager2)
    implementation(libs.preference.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.activity.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.window)
    implementation(libs.play.app.update.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

