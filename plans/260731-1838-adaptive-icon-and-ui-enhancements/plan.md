# Plan: Adaptive Launcher Icon & UI/UX Enhancements

Created: 2026-07-31
Status: ✅ Completed

## Overview
This plan addresses core user requirements for the **Batch Geotagging & File Range Manager** Android application:
1. **Adaptive Launcher Icon**: Design and implement modern Material 3 adaptive launcher icon assets (Foreground, Background, Monochrome, and xml configurations) based on the app's geotagging and file management capabilities.
2. **Flexible GPS Location Dialog ("Đặt Location mới")**: Support decimal formats with both dot `.` and comma `,`, single-line coordinate parsing (Google Maps output format `21,1573890, 106,1998193`), and a "Paste from Clipboard" button with UX optimization.
3. **File Range Dropdown with Thumbnails & Search ("Start File" & "End File")**: Enhance dropdown menus in the File Range tab with image thumbnails preview and a live search filter box.

## Tech Stack & Architecture
- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose (Material 3)
- **Image Loading**: Coil Compose (`io.coil-kt:coil-compose`)
- **Testing Framework**: JUnit 4 / Kotlin Test / Compose UI Test

## Phases Summary

| Phase | Name | Description | Status |
|-------|------|-------------|--------|
| 01 | [Adaptive Launcher Icon](phase-01-adaptive-launcher-icon.md) | Design & implement vector drawables and adaptive launcher icon configuration. | ✅ Completed |
| 02 | [Flexible Location Parser & Location Dialog](phase-02-location-parser-and-edit-dialog.md) | Implement GPS coordinate parser utility, clipboard paste, single-line format support & location dialog UI. | ✅ Completed |
| 03 | [File Range Dropdown with Search & Thumbnails](phase-03-file-range-dropdown-search-and-thumbnails.md) | Create searchable dropdown with Coil thumbnails for image files in Range Selection. | ✅ Completed |
| 04 | [Integration & End-to-End Verification](phase-04-integration-ui-polish-and-verification.md) | Integrate components, execute test suite, compile debug APK, and conduct UX review. | ✅ Completed |

---

## Quick Commands
- Run unit tests: `./gradlew test`
- Build debug APK: `./gradlew assembleDebug`
