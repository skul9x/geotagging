# Phase 02: Flexible Location Parser & Location Dialog Optimization
Status: ✅ Completed
Dependencies: None

## Objective
Implement a robust GPS coordinate parsing utility (`GpsCoordinateParser`) and revamp the "Đặt Location mới" (Set New Location) dialog in `HomeScreen.kt` to handle flexible coordinate inputs (dots `.`, commas `,`, combined single-line strings like Google Maps output), and clipboard paste functionality with optimized UI/UX.

## Requirements
### Functional
- **Comma & Dot Support**: Support decimal coordinates formatted with either dot `.` (e.g. `21.1573890`) or comma `,` (e.g. `21,1573890`).
- **Single-line String Parsing**: Accept single-line coordinate text copied directly from Google Maps or location tools (e.g., `"21,1573890, 106,1998193"`, `"21.1573890, 106.1998193"`, `"21,1573890 106,1998193"`) and automatically parse Latitude and Longitude.
- **Clipboard Paste Button**: Add a "Paste coordinates from clipboard" (Dán từ bộ nhớ tạm) button in the dialog using modern Compose `LocalClipboard` (`androidx.compose.ui.platform.LocalClipboard`) and CoroutineScope to read clip entry.
- **UI/UX Optimization**: Provide real-time coordinate validation feedback, quick clear action, single-line quick input box alongside explicit Lat/Long fields, and improved Material 3 layout styling.

### Non-Functional
- Input parsing must be error-resilient and never crash on invalid inputs or malformed clipboard strings.
- Smart parsing logic: Extract numeric pairs prior to decimal normalization so strings like `"21,1573890, 106,1998193"` (where `,` acts as both decimal point and separator) are correctly parsed into Lat `21.1573890` and Long `106.1998193`.
- Latitude must be restricted within [-90.0, 90.0] and Longitude within [-180.0, 180.0].

## Implementation Steps
1. Create utility `com.skul9x.geotagging.utils.GpsCoordinateParser`:
   - `parseSingleLineCoordinates(input: String): Pair<Double, Double>?` (smart regex-based extraction splitting coordinates before decimal normalization).
   - `parseCoordinates(latStr: String, longStr: String): Pair<Double, Double>?`.
2. Update `EditLocationDialog` in `app/src/main/java/com/skul9x/geotagging/ui/home/HomeScreen.kt`:
   - Add single-line string input field with auto-parsing on change.
   - Add "Paste from Clipboard" button utilizing `LocalClipboard` + CoroutineScope.
   - Update individual Latitude and Longitude text fields with bidirectional synchronization and comma/dot parsing support.
   - Add real-time validation badges and improved action buttons.

## Files to Create/Modify
- [NEW] [GpsCoordinateParser.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/utils/GpsCoordinateParser.kt) - Coordinate parsing utility logic
- [MODIFY] [HomeScreen.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/main/java/com/skul9x/geotagging/ui/home/HomeScreen.kt) - Refactored `EditLocationDialog` component
- [NEW] [GpsCoordinateParserTest.kt](file:///home/skul9x/Desktop/Test_code/geotagging-main/app/src/test/java/com/skul9x/geotagging/utils/GpsCoordinateParserTest.kt) - Unit test suite for parser

## Test Criteria & Verification Plan
### Detailed File-Based Tests
- **Unit Test File**: `app/src/test/java/com/skul9x/geotagging/utils/GpsCoordinateParserTest.kt`
  - Test case: `parse_standardDotCoordinates_returnsCorrectDoublePair()` (e.g. `"21.1573890"`, `"106.1998193"`).
  - Test case: `parse_commaDecimalCoordinates_returnsCorrectDoublePair()` (e.g. `"21,1573890"`, `"106,1998193"`).
  - Test case: `parse_singleLineGoogleMapsFormatWithCommas_returnsCorrectDoublePair()` (e.g. `"21,1573890, 106,1998193"`).
  - Test case: `parse_singleLineGoogleMapsFormatWithDots_returnsCorrectDoublePair()` (e.g. `"21.1573890, 106.1998193"`).
  - Test case: `parse_singleLineSpaceSeparated_returnsCorrectDoublePair()` (e.g. `"21.1573890 106.1998193"`).
  - Test case: `parse_outOfRangeCoordinates_returnsNull()` (e.g. Lat `95.0`, Long `200.0`).
  - Test case: `parse_invalidText_returnsNull()` (e.g. `"invalid string"`).
- Run execution: `./gradlew test --tests "com.skul9x.geotagging.utils.GpsCoordinateParserTest"`

---
Next Phase: [Phase 03: File Range Dropdown with Search & Thumbnails](phase-03-file-range-dropdown-search-and-thumbnails.md)
