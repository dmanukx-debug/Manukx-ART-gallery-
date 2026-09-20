package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.UserEntity
import com.example.ui.components.GalleryTopBar
import com.example.ui.theme.ManukxArtTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun gallery_topbar_screenshot() {
        val testUser = UserEntity(
            userId = "user_master",
            username = "elena_rostova",
            displayName = "Elena Rostova",
            bio = "Master Draftsman",
            avatarUrl = ""
        )

        composeTestRule.setContent {
            ManukxArtTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GalleryTopBar(
                        currentUser = testUser,
                        isDarkTheme = true,
                        onToggleTheme = {},
                        onOpenAuth = {},
                        onOpenAdmin = {},
                        onSearchClick = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
