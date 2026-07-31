# Phase 02: Scoped Storage & MediaStore Write Permission Handling
Status: ✅ Completed
Dependencies: Phase 01

## Objective
Implement Scoped Storage write permission handling in `HomeViewModel` and `HomeScreen`. Handle read-only PhotoPicker URIs, MediaStore write requests via `MediaStore.createWriteRequest` / `RecoverableSecurityException`, and SAF folder tree write permissions.

## Requirements
### Functional
- Handle `ExifWriteResult.PermissionDenied` in `HomeViewModel.updateLocationForImages`.
- For MediaStore URIs on Android 10+ (API 29+), emit a `HomeUiEvent.RequestWritePermission(intentSender)` when write permission is required.
- For SAF tree folder picking (`loadImagesFromFolder`), ensure `FLAG_GRANT_WRITE_URI_PERMISSION` is requested alongside `FLAG_GRANT_READ_URI_PERMISSION`.
- Display actionable UI feedback when Photo Picker read-only URIs cannot be written directly without user permission prompt.

### Non-Functional
- Graceful degradation when permission is denied by the user.
- Compliance with Android Scoped Storage guidelines.

## Implementation Steps
1. [x] Update `HomeViewModel.kt`:
   - Add `HomeUiEvent.RequestWritePermission(val intentSender: IntentSender)` to `HomeUiEvent`.
   - Update `loadImagesFromFolder` to request `FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION`.
   - Update `updateLocationForImages` to inspect `ExifWriteResult` and dispatch permission requests or error snackbars.
2. [x] Update `HomeScreen.kt`:
   - Add `rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult())` to launch system write permission dialog when triggered.
   - Re-attempt location edit upon permission grant result.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/geotagging/ui/home/HomeViewModel.kt` - [MODIFY] Handle write permissions and update location flow.
- `app/src/main/java/com/skul9x/geotagging/ui/home/HomeScreen.kt` - [MODIFY] Register IntentSender result launcher for write permissions.

## Test Criteria (File-Based Unit Tests)
- [x] Update / Create unit tests in `app/src/test/java/com/skul9x/geotagging/ui/home/HomeViewModelTest.kt`:
  - `test_updateLocation_permissionDenied_emitsPermissionEventOrSnackbar`: Test `updateLocationForImages` state flow when write permission fails.
  - `test_loadFolder_requestsWritePermissions`: Test folder tree permission flags include write permission.

---
Next Phase: [Phase 03: Integration, UX Error Feedback, and End-to-End Verification](file:///home/skul9x/Desktop/Test_code/geotagging-main/plans/260731-1918-scoped-storage-exif-fix/phase-03-integration-and-verification.md)
