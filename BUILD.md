# Build & Deployment Guide - Kazi Agrotech Frontend

This standalone frontend project is pre-configured with in-memory state management and mock data. It builds independently without any remote Firebase or Google services setup.

---

## 1. Quick APK Build via GitHub Actions

1. Initialize git and push to your new GitHub repository:
   ```bash
   git init
   git add .
   git commit -m "Kazi Agrotech Presentation Version"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo-name>.git
   git push -u origin main
   ```
2. Navigate to the **Actions** tab on your GitHub repository.
3. Select the **Build Android APK** workflow.
4. Click **Run workflow** (or wait for the automatic push trigger).
5. Once completed, scroll down to **Artifacts** and download `kazi-agrotech-debug-apk`.

---

## 2. Local Build (Android Studio or CLI)

### CLI (Windows PowerShell):
```powershell
.\gradlew.bat assembleDebug -x test
```

### CLI (Linux / macOS):
```bash
chmod +x ./gradlew
./gradlew assembleDebug -x test
```

The generated APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 3. Project Configuration

- **Package Name**: `com.aistudio.kaziagro.poultr`
- **Compile SDK**: 36
- **Min SDK**: 24
- **Keystore**: Pre-configured `debug.keystore` included in repository root for instant compilation.
