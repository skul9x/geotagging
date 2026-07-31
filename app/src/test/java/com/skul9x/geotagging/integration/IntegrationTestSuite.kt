package com.skul9x.geotagging.integration

import com.skul9x.geotagging.ui.FileRangeSearchTest
import com.skul9x.geotagging.ui.LauncherIconResourceTest
import com.skul9x.geotagging.ui.home.HomeViewModelTest
import com.skul9x.geotagging.ui.range.FileOperationsHelperTest
import com.skul9x.geotagging.ui.range.FileRangeScreenStateTest
import com.skul9x.geotagging.ui.range.FileRangeViewModelTest
import com.skul9x.geotagging.utils.FileRangeFilterTest
import com.skul9x.geotagging.utils.GpsCoordinateParserTest
import com.skul9x.geotagging.utils.NaturalOrderComparatorTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    FileRangeE2EIntegrationTest::class,
    FileRangeSearchTest::class,
    LauncherIconResourceTest::class,
    FileOperationsHelperTest::class,
    FileRangeScreenStateTest::class,
    FileRangeViewModelTest::class,
    FileRangeFilterTest::class,
    GpsCoordinateParserTest::class,
    NaturalOrderComparatorTest::class,
    HomeViewModelTest::class
)
class IntegrationTestSuite
