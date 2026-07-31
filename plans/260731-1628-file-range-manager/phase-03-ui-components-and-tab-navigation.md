# Phase 03: UI Components & Tab Navigation Integration

Status: ✅ Completed  
Dependencies: Phase 01, Phase 02  


## Objective
Refactor app main navigation to incorporate a top/bottom TabBar and build a modern Material 3 `FileRangeScreen` UI featuring source/destination pickers, range selection dropdowns, operation mode toggles, subfolder input, and interactive file list previews.

---

## Requirements

### Functional
- [ ] Implement `MainTabScreen` with Material 3 NavigationBar / NavigationRail / TabRow:
  - **Tab 1**: "Batch Geotag" (`HomeScreen`)
  - **Tab 2**: "File Range" (`FileRangeScreen`)
- [ ] Build `FileRangeScreen` layout:
  - **Directory Pickers Section**: Cards for Source Directory & Destination Directory with status indicators.
  - **Range Configuration Card**: Start File dropdown/picker & End File dropdown/picker with quick range select buttons.
  - **Operation Controls Section**:
    - Segmented Button / Radio Group for **Copy** vs **Move**.
    - Switch & TextField for **"Into New Subfolder"** (with auto-enter option).
  - **Preview & Execution Section**:
    - Summary chip (`Selected: 4 files / 15.2 MB`).
    - File Preview Grid/List displaying filenames with highlighted range selection bounds.
    - Floating Action Button or Action Button to trigger Copy/Move execution.
- [ ] Integrate SAF Document Tree pickers using `rememberLauncherForActivityResult`.

### UX & Aesthetic Standards
- [ ] Premium Material 3 styling (dynamic colors, smooth rounded corners `16.dp`, glassmorphism top bar, micro-animations).
- [ ] Quick selection capabilities: Tapping any file in the preview grid allows setting it instantly as "Start File" or "End File".
- [ ] Distinct visual badge tags for Copy (Primary color) vs Move (Tertiary / Warning color).

---

## Files to Create / Modify

- `[MODIFY]` [MainActivity.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/MainActivity.kt) - Set root content to `MainTabScreen`.
- `[NEW]` [MainTabScreen.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/ui/navigation/MainTabScreen.kt) - Tab navigation container holding HomeScreen and FileRangeScreen.
- `[NEW]` [FileRangeScreen.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/ui/range/FileRangeScreen.kt) - Main composable screen for File Range Manager.
- `[NEW]` [FileRangeComponents.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/ui/range/components/FileRangeComponents.kt) - Reusable components (DirectoryPickerCard, RangeSelectorCard, OperationModeToggle, NewFolderOptionCard, FilePreviewList).

---

## Verification & Detailed Tests

Create Unit / UI Component State Test files under `app/src/test/java/com/skul9x/geotagging/ui/range/`:

### 1. `FileRangeScreenStateTest.kt`
- [ ] **Test Case 1 (`testUiStateRenderingDefaults`)**: Verify default state renders empty directory placeholders and disables execute button until source & target are selected.
- [ ] **Test Case 2 (`testRangeSelectionHighlighting`)**: Verify files falling within `[StartFile, EndFile]` are marked with `isSelectedInRange = true`.
- [ ] **Test Case 3 (`testTabNavigationSwitching`)**: Verify switching between Tab 0 (Geotag) and Tab 1 (File Range) preserves state in both viewmodels.

---

Next Phase: [phase-04-ux-polish-and-end-to-end-verification.md](file:///home/skul9x/Desktop/Test_code/geotagging-main/plans/260731-1628-file-range-manager/phase-04-ux-polish-and-end-to-end-verification.md)
