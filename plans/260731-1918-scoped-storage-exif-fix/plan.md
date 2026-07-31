# Plan: Scoped Storage EXIF Location Edit Fix
Created: 2026-07-31 19:18
Status: ✅ Completed

## Overview
Fix EXIF GPS location writing failure caused by Android Scoped Storage and read-only URI restrictions (Photo Picker & MediaStore URIs). Implement robust error reporting in `ExifUtils`, Scoped Storage write permission prompts (`MediaStore.createWriteRequest`), and SAF tree URI write permissions.

## Root Cause Summary
1. Photo Picker grants **read-only** URI permissions (`FLAG_GRANT_READ_URI_PERMISSION`).
2. Calling `contentResolver.openFileDescriptor(uri, "rw")` on a read-only URI throws `SecurityException`.
3. On Android 10+ (API 29+), modifying MediaStore files owned by other applications requires `MediaStore.createWriteRequest` (Android 11+) or `RecoverableSecurityException` handling (Android 10).
4. `ExifUtils.writeLocation` currently catches all exceptions silently with `e.printStackTrace()` and returns `false`, concealing the exact failure root cause from the UI.

## Tech Stack & APIs
- `androidx.exifinterface:exifinterface`
- `android.provider.MediaStore.createWriteRequest`
- `android.app.RecoverableSecurityException`
- AndroidX ViewModel & Compose StateFlow

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | ExifUtils Error Diagnostic & Result Refactoring | ✅ Completed | 100% |
| 02 | Scoped Storage & MediaStore Write Permission Handling | ✅ Completed | 100% |
| 03 | Integration, UX Error Feedback, and End-to-End Verification | ✅ Completed | 100% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Run Tests: `./gradlew test`

