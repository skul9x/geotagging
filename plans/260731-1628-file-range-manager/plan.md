# Implementation Plan - File Range Manager (Copy / Move by A-Z Range)

**Target Feature**: A dedicated tab for batch copying and moving files selected by file name range (sorted A-Z naturally) into a destination directory (with optional subfolder creation).

---

## 📊 Feature Overview

1. **Tab Navigation Bar (UI Architecture Refactor)**:
   - Add tab navigation in `MainActivity` / top layout with Material 3 NavigationBar.
   - **Tab 1: "Batch Geotag"** (Existing EXIF geotagging screen).
   - **Tab 2: "File Range Manager"** (New screen for range-based file copy/move).

2. **File Range Operations**:
   - **Directory Pickers**: Select Source Directory and Destination Directory using Storage Access Framework (SAF `OpenDocumentTree`).
   - **Natural A-Z Sorting**: Sort directory files naturally (e.g. `abc1`, `abc2`, ..., `abc9`, `abc10`).
   - **Range Selection**: Pick/enter Start File (e.g. `abc1`) and End File (e.g. `abc4`).
   - **Operation Type**: Toggle between **Copy** and **Move**.
   - **New Subfolder Option**: Checkbox & input to create a new target subfolder inside the destination directory, with option to set/enter the new folder.
   - **Progress & Feedback**: Real-time progress bar, success/failure counts, and detailed operation feedback.

3. **UI / UX Enhancements**:
   - Modern Material 3 cards, status badges, clean form inputs with drop-downs / autocomplete for Start/End files.
   - Live preview of selected file count & total byte size.
   - Responsive grid/list view with visual selection bounds.

---

## 📁 Plan Phases & Milestones

| Phase | Description | Key Deliverables | Detailed Test File | Status |
|---|---|---|---|---|
| **Phase 01** | Core Domain Models & Natural Sorting Engine | `FileItem.kt`, `NaturalOrderComparator.kt`, `FileRangeFilter.kt` | `NaturalOrderComparatorTest.kt`, `FileRangeFilterTest.kt` | ⬜ Pending |
| **Phase 02** | ViewModel & SAF File Operations Engine | `FileRangeViewModel.kt`, `FileOperationsHelper.kt` | `FileRangeViewModelTest.kt`, `FileOperationsHelperTest.kt` | ⬜ Pending |
| **Phase 03** | UI Components & Tab Navigation Integration | `MainTabScreen.kt`, `FileRangeScreen.kt`, `FileRangeComponents.kt` | `FileRangeScreenStateTest.kt` | ⬜ Pending |
| **Phase 04** | UX Polish & End-to-End Verification | Animations, progress dialog, snackbars, local file system test | `FileRangeE2EIntegrationTest.kt` | ⬜ Pending |

---

## 🚀 Quick Execution Guide

- Start Phase 1: `/code phase-01`
- Start Phase 2: `/code phase-02`
- Start Phase 3: `/code phase-03`
- Start Phase 4: `/code phase-04`
