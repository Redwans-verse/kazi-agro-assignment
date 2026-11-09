plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  val propVersionCode = project.findProperty("VERSION_CODE")?.toString()?.toIntOrNull()
      ?: System.getenv("VERSION_CODE")?.toIntOrNull()
      ?: 1
  val propVersionName = project.findProperty("VERSION_NAME")?.toString()
      ?: System.getenv("VERSION_NAME")
      ?: "1.0.0"

  defaultConfig {
    applicationId = "com.kaziagro.poultry"
    minSdk = 24
    targetSdk = 36
    versionCode = propVersionCode
    versionName = propVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val kaziKeystore = file("${rootDir}/kazi_keystore.jks")
      val myUploadKey = file("${rootDir}/my-upload-key.jks")
      val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
          ?: System.getenv("KEYSTORE_PATH")
      val keyFile = if (keystorePath != null) file(keystorePath) else if (kaziKeystore.exists()) kaziKeystore else myUploadKey

      if (keyFile.exists()) {
        storeFile = keyFile
        if (keyFile.name == "kazi_keystore.jks") {
          storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD") ?: "kaziagro123"
          keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "kaziagro"
          keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: "kaziagro123"
        } else {
          storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
              ?: System.getenv("KEYSTORE_PASSWORD")
              ?: System.getenv("STORE_PASSWORD")
              ?: "android"
          keyAlias = System.getenv("RELEASE_KEY_ALIAS")
              ?: System.getenv("KEY_ALIAS")
              ?: "upload"
          keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
              ?: System.getenv("KEY_PASSWORD")
              ?: "android"
        }
      } else {
        val rootDebugKeystore = file("${rootDir}/debug.keystore")
        if (rootDebugKeystore.exists()) {
          storeFile = rootDebugKeystore
          storePassword = "android"
          keyAlias = "androiddebugkey"
          keyPassword = "android"
        }
      }
    }
    create("debugConfig") {
      val rootDebugKeystore = file("${rootDir}/debug.keystore")
      if (rootDebugKeystore.exists()) {
        storeFile = rootDebugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.auth)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
