# Build & Execution Guide - Kazi Agrotech

Instructions for building and running the Kazi Agrotech Android application locally and via automated CI/CD.

---

## 1. Environment & Prerequisites

- **JDK Version**: OpenJDK / Eclipse Temurin 21
- **Android SDK**: Compile SDK 36, Target SDK 36, Min SDK 24 (Android 7.0+)
- **Build Tool**: Gradle 9.3.1 with Android Gradle Plugin 9.1.1
- **Application ID**: `com.kaziagro.poultry`

---

## 2. Building via Android Studio

1. Open **Android Studio** (Ladybug / Meerkat or newer recommended).
2. Select **Open** and choose the `kazi-agrotech-frontend` project directory.
3. Allow Gradle to synchronize dependencies.
4. To build the APK:
   - Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - Or click the **Run** button (green play icon) to launch directly on an emulator or physical device.

---

## 3. Building via Command Line (Gradle)

### Windows (PowerShell / Command Prompt):
```powershell
# Build Debug APK
.\gradlew.bat assembleDebug -x test

# Build Release APK
.\gradlew.bat assembleRelease -x test
```

### macOS / Linux:
```bash
chmod +x ./gradlew

# Build Debug APK
./gradlew assembleDebug -x test

# Build Release APK
./gradlew assembleRelease -x test
```

### Output APK Locations:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

---

## 4. Automated Builds (GitHub Actions)

This repository includes a continuous integration workflow (`.github/workflows/build-apk.yml`) that automatically compiles both Debug and Release APKs upon every push to the `main` branch.

To download the compiled APKs:
1. Open the repository on GitHub and click the **Actions** tab.
2. Select the latest completed workflow run (**Build Android APK**).
3. Scroll down to the **Artifacts** section at the bottom to download `kazi-agrotech-debug-apk` or `kazi-agrotech-release-apk`.
