# WATER PE NATIVE OVERLAY V1.1

Android companion overlay for Minecraft Bedrock, designed for Android 8.0+ (including Redmi 12C / Android 12).

## Included
- Draggable floating `W` bubble.
- Purple/black ClickGUI-inspired menu.
- Toggleable crosshair overlay.
- Clock HUD.
- Session timer HUD.
- Foreground service notification while running.
- No third-party Android libraries.

## Important scope
This is an Android overlay displayed above another app after the user grants **Display over other apps** permission. It is NOT an injected/native Minecraft client.

It does not load Java/Fabric modules, patch `libminecraftpe.so`, inspect Minecraft process memory, read in-game XYZ/FPS, modify player/camera/inventory/block/entity state, or bypass anti-cheat. It does not add features to a world. Its UI is independent of the Minecraft version, so it may be displayed over Bedrock 1.21.101 where Android permits overlays.

## Build in Android Studio
Requirements: Android SDK Platform 35, JDK 17 and Gradle 8.9. Open this folder as a project and select **Build > Build APK(s)**. Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Build APK without installing Android Studio on the phone
A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`:
1. Create a GitHub repository and upload the contents of this folder.
2. Open the repository's **Actions** tab.
3. Run **Build WATER PE Overlay APK** (or push to the `main` branch).
4. Open the completed workflow run and download the artifact `WATER-PE-OVERLAY-APK`.
5. Extract the artifact ZIP and install `app-debug.apk` on the phone.

A GitHub account and internet access are required for cloud building. The workflow has not been executed in this environment.

## Install and use
1. Install `app-debug.apk`.
2. Open **WATER PE Overlay**.
3. Tap **1. CẤP QUYỀN HIỂN THỊ** and allow **Display over other apps**.
4. Return to the app and tap **2. BẬT MENU NỔI**.
5. Open Minecraft Bedrock and tap the floating purple `W` bubble.
6. Toggle Crosshair, Clock HUD and Session timer.

On MIUI, if the overlay closes in the background, check app battery settings and allow background activity. Settings names can vary by MIUI version.

## Verification status
Project source and packaging are prepared. This environment does not have Android SDK/Gradle installed, so an APK could not be built or run here. The GitHub Actions workflow is included but has not been run.
