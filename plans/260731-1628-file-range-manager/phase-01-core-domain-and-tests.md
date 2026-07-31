# Phase 01: Core Domain Models & Natural Sorting Engine

Status: ✅ Completed  
Dependencies: None  
Completed At: 2026-07-31T09:39:35Z

## Objective
Implement core domain data models and natural alphanumeric sorting algorithms (A-Z with integer-aware comparison) to slice directory files accurately by Start and End file boundaries.

---

## Requirements

### Functional
- [x] Define `FileItem` data class representing files in a directory (URI, file name, size, mime-type, last modified, parent URI).
- [x] Implement `NaturalOrderComparator` to sort file names naturally (`abc1`, `abc2`, `abc9`, `abc10` instead of ASCII `abc1`, `abc10`, `abc2`).
- [x] Implement `FileRangeFilter` logic to extract a sublist of sorted files given a `startFileName` and `endFileName` (inclusive range).
- [x] Support case-insensitive search and partial prefix matching (fallback to nearest matching indices if exact name match is partial).

### Non-Functional
- [x] High performance sorting for directories containing up to 10,000 files (< 50ms execution time).
- [x] Zero memory leaks and immutable state representations.

---

## Files to Create / Modify

- `[NEW]` [FileItem.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/data/model/FileItem.kt) - Data model for file representation.
- `[NEW]` [NaturalOrderComparator.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/utils/NaturalOrderComparator.kt) - Comparator for natural alphanumeric string sorting.
- `[NEW]` [FileRangeFilter.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/utils/FileRangeFilter.kt) - Helper utility to filter sorted files within start & end bounds.

---

## Verification & Detailed Tests

Create Unit Test files under `app/src/test/java/com/skul9x/geotagging/data/`:

### 1. `NaturalOrderComparatorTest.kt`
- [x] **Test Case 1 (`testNaturalSortingAlphanumeric`)**: Verify `['abc10', 'abc1', 'abc2', 'abc9']` sorts to `['abc1', 'abc2', 'abc9', 'abc10']`.
- [x] **Test Case 2 (`testCaseInsensitiveSorting`)**: Verify `['ABC1', 'abc2', 'Abc3']` sorts in correct sequence regardless of case.
- [x] **Test Case 3 (`testComplexFileNames`)**: Verify filenames with extensions (`img_01.jpg`, `img_2.jpg`, `img_10.jpg`) sort as `img_01.jpg`, `img_2.jpg`, `img_10.jpg`.

### 2. `FileRangeFilterTest.kt`
- [x] **Test Case 1 (`testExtractValidRange`)**: Input list `['abc1', 'abc2', 'abc3', 'abc4', 'abc5']`, start `abc1`, end `abc4` -> Output `['abc1', 'abc2', 'abc3', 'abc4']` (4 items).
- [x] **Test Case 2 (`testStartFileGreaterThanEndFile`)**: Return empty list or auto-swap start/end bounds gracefully.
- [x] **Test Case 3 (`testPartialMatchingRange`)**: Test when exact name doesn't match extension, matching by prefix or exact base name.
- [x] **Test Case 4 (`testSingleFileRange`)**: Start and end are identical file names -> Output 1 item.

---

Next Phase: [phase-02-viewmodel-and-file-operations.md](file:///home/skul9x/Desktop/Test_code/geotagging-main/plans/260731-1628-file-range-manager/phase-02-viewmodel-and-file-operations.md)
