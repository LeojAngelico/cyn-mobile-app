plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cyn.mobile.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "cyn.mobile.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug")
        create("release") {
            // Note the second argument: providers
            val lp = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)

            val ksPath = lp.getProperty("keystoreFile") ?: System.getenv("KEYSTORE_FILE")
            val ksPass = lp.getProperty("storePassword") ?: System.getenv("STORE_PASSWORD")
            val alias  = lp.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
            val keyPass= lp.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")

            if (ksPath.isNullOrBlank() || ksPass.isNullOrBlank() || alias.isNullOrBlank() || keyPass.isNullOrBlank()) {
                throw GradleException("Missing release signing properties. Define keystoreFile, storePassword, keyAlias, keyPassword in local.properties or env.")
            }

            val f = file(ksPath)
            if (!f.exists()) {
                throw GradleException("Keystore file not found at: $ksPath")
            }

            storeFile = f
            storePassword = ksPass
            keyAlias = alias
            keyPassword = keyPass
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            // Always use the release signing config for all release variants (dev/alpha/prod)
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // Make Java and Kotlin targets consistent
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    // Optional: ensure the toolchain is Java 21 on all machines/CI
    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    applicationVariants.all {
        val appName = "CYN Mobile"
        val flavor = if (flavorName.isNullOrBlank()) "noflavor" else flavorName
        val verName = versionName
        val bt = buildType.name

        outputs.all {
            if (this is com.android.build.gradle.api.ApkVariantOutput) {
                this.outputFileName = "$appName-$verName-$flavor-$bt.apk"
            }
        }
    }


    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            isDefault = true
            applicationIdSuffix = ".dev"
            manifestPlaceholders["appName"] = "Dev - ${rootProject.name}"
            buildConfigField("String", "BASE_URL", "\"https://mobileapp.cynsolutions.com.ph/\"")
            buildConfigField("String", "BASE_TEST_URL", "\"https://cyn-initial-server-impan.ondigitalocean.app/\"")
            buildConfigField("String", "BASE_API_KEY", "\"Kb7mhanecfvduPRSRWqxJJvv7kecsn7s66YWtCMZ37sMTgetr58C7duBDTaKUdST\"")
        }
        create("alpha") {
            dimension = "env"
            applicationIdSuffix = ".alpha"
            manifestPlaceholders["appName"] = "Alpha - ${rootProject.name}"
            buildConfigField("String", "BASE_URL", "\"https://mobileapp.cynsolutions.com.ph/\"")
            buildConfigField("String", "BASE_TEST_URL", "\"https://cyn-initial-server-impan.ondigitalocean.app/\"")
            buildConfigField("String", "BASE_API_KEY", "\"Kb7mhanecfvduPRSRWqxJJvv7kecsn7s66YWtCMZ37sMTgetr58C7duBDTaKUdST\"")
        }
        create("prod") {
            dimension = "env"
            manifestPlaceholders["appName"] = "${rootProject.name}-v${defaultConfig.versionName}"
            buildConfigField("String", "BASE_URL", "\"https://mobileapp.cynsolutions.com.ph/\"")
            buildConfigField("String", "BASE_TEST_URL", "\"https://cyn-initial-server-impan.ondigitalocean.app/\"")
            buildConfigField("String", "BASE_API_KEY", "\"Kb7mhanecfvduPRSRWqxJJvv7kecsn7s66YWtCMZ37sMTgetr58C7duBDTaKUdST\"")
        }
    }
}

configurations.configureEach {
    resolutionStrategy {
        force("junit:junit:4.13.2")
    }
}


dependencies {
    // Optional: reinforce via constraints (helps Gradle pick the right version on all classpaths)
    constraints {
        testImplementation(libs.junit)
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    // Removed: implementation(libs.javapoet)
    // If you really need JavaPoet for compile-time utilities (not runtime), prefer:
    // compileOnly(libs.javapoet)

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp-urlconnection")


    implementation(libs.androidx.security.crypto)

    implementation(libs.datastore.prefs)      // Option B
    implementation(libs.tink.android)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Retrofit Moshi converter
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.materialtoast)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.swiperefreshlayout)




}