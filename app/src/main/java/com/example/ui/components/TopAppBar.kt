package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudyAccentAmber
import com.example.ui.theme.StudyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTopAppBar(
    streakCount: Int,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenSettings: () -> Unit,
    onStreakClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Study30",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Grade 8",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = "30-Day Exam Strategy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Streak counter badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(StudyAccentAmber.copy(alpha = 0.15f))
                    .border(1.dp, StudyAccentAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onStreakClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("streak_counter_badge"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = StudyAccentAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$streakCount d",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = StudyAccentAmber
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Quick Timer Launcher
            IconButton(
                onClick = onOpenTimer,
                modifier = Modifier.testTag("action_open_timer")
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = "Study Session Timer",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Theme Switcher
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("action_toggle_theme")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme"
                )
            }

            // Settings
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("action_open_settings")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
