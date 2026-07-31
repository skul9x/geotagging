# Phase 02: ViewModel & SAF File Operations Engine

Status: ✅ Completed  
Dependencies: Phase 01  

## Objective
Implement high-performance asynchronous file copy/move operations via Android Storage Access Framework (`DocumentsContract` API with `DocumentFile` fallback) and build `FileRangeViewModel` to manage UI state, directory loading, range calculations, and execution progress.

---

## Requirements

### Functional
- [x] Create `FileOperationMode` enum (`COPY`, `MOVE`).
- [x] Implement high-performance directory reader using `ContentResolver.query` with `DocumentsContract.buildChildDocumentsUriUsingTree()` projection to retrieve all file names, URIs, sizes, and flags in a single batch IPC call.
- [x] Implement `FileOperationsHelper`:
  - Utilize `DocumentsContract.copyDocument()` and `DocumentsContract.moveDocument()` (API 24+) when supported by the provider for near-instant native file operations.
  - Provide automatic fallback to buffered IO stream copying (`openInputStream` / `openOutputStream`) and original file deletion for cross-provider moves.
- [x] Support subfolder creation inside target directory (`DocumentsContract.createDocument` or `DocumentFile.createDirectory`).
- [x] Support option to set/enter into the newly created folder after operation completes.
- [x] Provide real-time progress callbacks (copied bytes/files count, total files, progress fraction 0.0 .. 1.0).
- [x] Build `FileRangeViewModel` with StateFlow UI state (`FileRangeUiState`) and event channel (`FileRangeUiEvent`).

### Non-Functional
- [x] Execute file operations asynchronously on `Dispatchers.IO` thread with non-blocking UI.
- [x] Avoid loop IPC overhead by querying child document projection instead of calling `DocumentFile.listFiles()` repeatedly.
- [x] Provide atomic file cleanup (delete source only after copy successfully finishes in MOVE mode).
- [x] Handle IO exceptions safely (storage full, read-only permissions) with per-file status reporting.

---

## Files to Create / Modify

- `[NEW]` [FileOperationMode.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/data/model/FileOperationMode.kt) - Enum for Copy/Move operation types.
- `[NEW]` [FileOperationsHelper.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/utils/FileOperationsHelper.kt) - Asynchronous SAF file copy, move, and subfolder creation utility using `DocumentsContract`.
- `[NEW]` [FileRangeViewModel.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/ui/range/FileRangeViewModel.kt) - ViewModel handling state, range bounds, directory URIs, and operation progress.

---

## Verification & Detailed Tests

Create Unit Test files under `app/src/test/java/com/skul9x/geotagging/ui/range/`:

### 1. `FileRangeViewModelTest.kt`
- [x] **Test Case 1 (`testSelectSourceDirectoryLoadsAndSortsFiles`)**: Verify loading directory populates files sorted A-Z automatically.
- [x] **Test Case 2 (`testRangeSelectionUpdatesSelectedFileList`)**: Setting start = `abc1` and end = `abc4` updates `selectedCount` to 4 and calculates total size accurately.
- [x] **Test Case 3 (`testOperationModeToggle`)**: Toggling mode between `COPY` and `MOVE` updates UI state correctly.
- [x] **Test Case 4 (`testNewSubfolderOptionState`)**: Enabling "Create & Enter New Folder" updates target URI location.

### 2. `FileOperationsHelperTest.kt`
- [x] **Test Case 1 (`testLocalFileCopyAndMoveMock`)**: Test streams logic copying content from source input stream to target output stream.
- [x] **Test Case 2 (`testProgressCallbackDispatch`)**: Verify progress callbacks emit fractional values from 0.0 to 1.0 during file batch operations.

---

Next Phase: [phase-03-ui-components-and-tab-navigation.md](file:///home/skul9x/Desktop/Test_code/geotagging-main/plans/260731-1628-file-range-manager/phase-03-ui-components-and-tab-navigation.md)
