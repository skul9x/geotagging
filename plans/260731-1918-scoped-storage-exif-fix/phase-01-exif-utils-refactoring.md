# Phase 01: ExifUtils Error Diagnostic & Result Refactoring
Status: ✅ Completed
Dependencies: None

## Objective
Refactor `ExifUtils` to provide detailed error diagnostics when reading and writing EXIF location metadata. Replace simple boolean return types with a structured result model (`ExifResult`), add explicit logging, and ensure safe file descriptor closing.

## Requirements
### Functional
- Refactor `ExifUtils.writeLocation` to return `ExifWriteResult` indicating:
  - `Success`: Metadata successfully updated.
  - `PermissionDenied(exception)`: SecurityException when opening file in `"rw"` mode.
  - `FileNotFound(exception)`: Invalid or inaccessible URI.
  - `WriteFailed(exception)`: Failure during `exif.saveAttributes()` or IO error.
- Log error trace details using `android.util.Log` for easier debugging in Logcat.
- Preserve existing `readLocation` behavior while capturing failure diagnostics.

### Non-Functional
- Null safety and exception isolation.
- Zero breaking changes to existing model interfaces.

## Implementation Steps
1. [x] Define `ExifWriteResult` sealed interface in `ExifUtils.kt`.
2. [x] Refactor `ExifUtils.writeLocation` to catch specific exceptions (`SecurityException`, `FileNotFoundException`, `IOException`) and return the corresponding `ExifWriteResult`.
3. [x] Add structured `Log.e` logging calls with descriptive tags (`ExifUtils`).

## Files to Create/Modify
- `app/src/main/java/com/skul9x/geotagging/utils/ExifUtils.kt` - [MODIFY] Refactor return types and error handling.

## Test Criteria (File-Based Unit Tests)
- [x] Create `app/src/test/java/com/skul9x/geotagging/utils/ExifUtilsTest.kt`:
  - `test_writeLocation_permissionDenied_returnsPermissionDeniedResult`: Mock `ContentResolver` throwing `SecurityException` on `openFileDescriptor` with `"rw"`.
  - `test_writeLocation_fileNotFound_returnsFileNotFoundResult`: Mock `ContentResolver` returning `null` or `FileNotFoundException`.
  - `test_writeLocation_success_returnsSuccessResult`: Mock successful descriptor open and write operation.

---
Next Phase: [Phase 02: Scoped Storage & MediaStore Write Permission Handling](file:///home/skul9x/Desktop/Test_code/geotagging-main/plans/260731-1918-scoped-storage-exif-fix/phase-02-scoped-storage-write-permissions.md)
