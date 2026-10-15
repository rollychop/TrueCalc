import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.gms.google.services)
}


kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("21")
    }
}

android {
    namespace = "com.brohit.truecalc"
    compileSdk = 37



    defaultConfig {
        applicationId = "com.brohit.truecalc"
        minSdk = 26
        targetSdk = 37
        // CI overrides these for GitHub releases: -PversionCode=… -PversionName=…
        versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 5
        versionName = (project.findProperty("versionName") as String?) ?: "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PLAY_PACKAGE", "\"com.brohit.truecalc\"")
        buildConfigField(
            "String", "RELEASES_URL", "\"https://github.com/rollychop/TrueCalc/releases/latest\""
        )
    }

    signingConfigs {
        // Signs the GitHub distribution. Uses CI secrets when set, else the committed keystore.
        create("github") {
            val props = Properties().apply {
                val file = rootProject.file("keystore/github-keystore.properties")
                if (file.exists()) file.inputStream().use { load(it) }
            }
            fun value(env: String, key: String): String? = System.getenv(env) ?: props.getProperty(key)
            storeFile = rootProject.file(value("SIGNING_STORE_FILE", "storeFile") ?: "keystore/truecalc-github.jks")
            storePassword = value("SIGNING_STORE_PASSWORD", "storePassword")
            keyAlias = value("SIGNING_KEY_ALIAS", "keyAlias")
            keyPassword = value("SIGNING_KEY_PASSWORD", "keyPassword")
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        // Google Play build: same package as the published app.
        create("play") {
            dimension = "distribution"
            buildConfigField("Boolean", "IS_PLAY_BUILD", "true")
        }
        // GitHub Releases build: separate package, so it installs alongside the Play app.
        create("github") {
            dimension = "distribution"
            applicationIdSuffix = ".github"
            versionNameSuffix = "-github"
            buildConfigField("Boolean", "IS_PLAY_BUILD", "false")
            signingConfig = signingConfigs.getByName("github")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
//    ksp {
//        arg("room.schemaLocation", "$projectDir/schemas")
//    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //
    implementation(libs.material)

    //icons
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.splash.api)
    implementation(libs.androidx.navigation.compose)


    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    ksp(libs.androidx.room.compiler)

    //work manager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    implementation(libs.kotlinx.serialization.json)

    //hilt
    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation)

    //coroutine
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    //retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    //coil
    implementation(libs.coil)

    //charts
    implementation(libs.compose.charts)
    implementation(libs.compose.markdown)

    //fonts
    implementation(libs.androidx.ui.text.google.fonts)

    //lottie
    implementation(libs.lottie.compose)

    //in app update
    implementation(libs.app.update.ktx)



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}