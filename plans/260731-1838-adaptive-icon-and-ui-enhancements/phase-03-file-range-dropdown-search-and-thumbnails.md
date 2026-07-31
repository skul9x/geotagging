# Phase 03: File Range Dropdown with Search & Image Thumbnails
Status: ✅ Completed
Completed At: 2026-07-31T18:57:00+07:00
Dependencies: None

## Objective
Enhance the "Start File" and "End File" selection UI in the File Range tab (`FileRangeComponents.kt`) by adding live image thumbnails for image files and an embedded search box inside expanded dropdown menus/pickers.

## Requirements
### Functional
- **Image Thumbnails in Selection List**: Display image previews using Coil (`AsyncImage`) for image files (`.jpg`, `.jpeg`, `.png`, `.webp`, `.heic`, etc.) in the list items when selecting "Start File" or "End File". Show appropriate file fallback icons for non-image files.
- **Search Box Filter**: Embed a search `OutlinedTextField` at the top of the expanded dropdown dialog/picker, enabling real-time substring filtering of file names.
- **Natural Order & Selection UX**: Maintain natural order sorting while filtering, highlight the currently selected file, and support clear search functionality.
- **Smooth Performance**: Efficiently handle lists with hundreds of files without lagging during scroll or filtering.

### Non-Functional
- Asynchronous image loading using Coil `AsyncImage` with explicit target size constraint (`size(120, 120)` or `size(40.dp)`) to prevent excessive bitmap memory usage on large file lists.
- Memoize filtered file lists with `remember(query, sourceFiles)` or `derivedStateOf` to prevent re-filtering during UI recompositions.
- Clean responsive UI consistent with Material 3 design tokens.

## Implementation Steps
1. Create searchable file picker component `SearchableFileDropdown` / `FileSelectionDialog` in `FileRangeComponents.kt`:
   - State holding query string for filtering.
   - Top search input bar with search leading icon and clear trailing button.
   - `LazyColumn` listing filtered `FileItem`s.
   - Each item includes Coil `AsyncImage` thumbnail (with clip shape and size ~40dp), filename, and file size.
2. Integrate `SearchableFileDropdown` into `RangeSelectorCard` in `FileRangeComponents.kt` for "Start File" and "End File" controls.
3. Update `FileRangeScreen.kt` if necessary to support smooth dialog/bottom sheet presentation for file selection.

## Files to Create/Modify
- [MODIFY] [FileRangeComponents.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/ui/range/components/FileRangeComponents.kt) - Enhanced dropdown component with search and thumbnails
- [NEW] [FileRangeSearchTest.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/test/java/com/skul9x/geotagging/ui/FileRangeSearchTest.kt) - Unit test suite for search filter logic

## Test Criteria & Verification Plan
### Detailed File-Based Tests
- **Unit Test File**: `app/src/test/java/com/skul9x/geotagging/ui/FileRangeSearchTest.kt`
  - Test case: `filterFiles_emptyQuery_returnsFullList()`
  - Test case: `filterFiles_matchingQuery_returnsFilteredSubset()` (e.g. searching `"002"` filters `IMG_002.jpg`).
  - Test case: `filterFiles_caseInsensitiveQuery_returnsMatches()` (e.g. searching `"img"` matches `IMG_001.jpg`).
  - Test case: `filterFiles_noMatch_returnsEmptyList()`
  - Test case: `isImageFile_detectsValidImageExtensions()` (verifying extension helper for thumbnails rendering).
- Run execution: `./gradlew test --tests "com.skul9x.geotagging.ui.FileRangeSearchTest"`

---
Next Phase: [Phase 04: Integration & End-to-End Verification](phase-04-integration-ui-polish-and-verification.md)
