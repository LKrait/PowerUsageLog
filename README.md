# Power Usage Log

Android app for recording electrical power readings for up to 10 feeders.

## Build without Android Studio

This repository includes a GitHub Actions workflow.

1. Create a GitHub repository.
2. Upload all files and folders from this project.
3. Open **Actions** in GitHub.
4. Run **Build Android APK**.
5. When the workflow finishes, open the workflow run and download the **PowerUsageLog-debug-apk** artifact.
6. Transfer the APK to an Android phone and install it.

The app is built with Kotlin and Jetpack Compose.

## Main features

- Dashboard for 10 feeders
- Feeder name and area/description
- Current Power (kW)
- Power Factor
- Total Usage (kWh)
- Date and time
- Optional notes
- Input validation
- Saved readings stored locally on the phone
- View Logs screen
- No internet connection is required for normal operation

## Default feeder names

1. Feeder 1 — Production Line 1
2. Feeder 2 — Production Line 2
3. Feeder 3 — Compressors
4. Feeder 4 — HVAC
5. Feeder 5 — Lighting
6. Feeder 6 — Water Pump
7. Feeder 7 — Workshop
8. Feeder 8 — Office Building
9. Feeder 9 — Warehouse
10. Feeder 10 — Spare / Other

These can be changed in `app/src/main/java/com/example/powerusagelog/MainActivity.kt`.
