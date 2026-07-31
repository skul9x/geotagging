package com.skul9x.geotagging.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.skul9x.geotagging.ui.home.HomeScreen
import com.skul9x.geotagging.ui.range.FileRangeScreen

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    BATCH_GEOTAG(
        title = "Batch Geotag",
        selectedIcon = Icons.Filled.PinDrop,
        unselectedIcon = Icons.Outlined.PinDrop
    ),
    FILE_RANGE(
        title = "File Range",
        selectedIcon = Icons.Filled.FolderCopy,
        unselectedIcon = Icons.Outlined.FolderCopy
    )
}

@Composable
fun MainTabScreen(
    modifier: Modifier = Modifier,
    initialTab: Int = 0
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(initialTab) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                MainTab.values().forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen()
                1 -> FileRangeScreen()
            }
        }
    }
}
