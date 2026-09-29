package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.StudyTab

@Composable
fun StudyBottomNav(
    currentTab: StudyTab,
    weakTopicsCount: Int,
    onTabSelected: (StudyTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = currentTab == StudyTab.HOME,
            onClick = { onTabSelected(StudyTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StudyTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentTab == StudyTab.PLANNER,
            onClick = { onTabSelected(StudyTab.PLANNER) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StudyTab.PLANNER) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Planner"
                )
            },
            label = { Text("Planner", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_item_planner")
        )

        NavigationBarItem(
            selected = currentTab == StudyTab.SUBJECTS,
            onClick = { onTabSelected(StudyTab.SUBJECTS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StudyTab.SUBJECTS) Icons.Filled.LibraryBooks else Icons.Outlined.LibraryBooks,
                    contentDescription = "Subjects"
                )
            },
            label = { Text("Subjects", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_item_subjects")
        )

        NavigationBarItem(
            selected = currentTab == StudyTab.PROGRESS,
            onClick = { onTabSelected(StudyTab.PROGRESS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StudyTab.PROGRESS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Progress"
                )
            },
            label = { Text("Progress", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_item_progress")
        )

        NavigationBarItem(
            selected = currentTab == StudyTab.REVISION,
            onClick = { onTabSelected(StudyTab.REVISION) },
            icon = {
                BadgedBox(
                    badge = {
                        if (weakTopicsCount > 0) {
                            Badge {
                                Text("$weakTopicsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == StudyTab.REVISION) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                        contentDescription = "Revision"
                    )
                }
            },
            label = { Text("Revision", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.testTag("nav_item_revision")
        )
    }
}
