package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GalleryBottomNav
import com.example.ui.components.GalleryTopBar
import com.example.ui.screens.*
import com.example.ui.theme.ManukxArtTheme
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GalleryViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            val notifications by viewModel.notifications.collectAsState()
            val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

            val inspectedPost by viewModel.inspectedPost.collectAsState()
            val commentingPostId by viewModel.commentingPostId.collectAsState()
            val purchasingItem by viewModel.purchasingItem.collectAsState()
            val showAuthDialog by viewModel.showAuthDialog.collectAsState()
            val showAdminPanel by viewModel.showAdminPanel.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.toastMessage.collectLatest { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }

            ManukxArtTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showAdminPanel) {
                        AdminScreen(
                            viewModel = viewModel,
                            onClose = { viewModel.toggleAdminPanel(false) }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                GalleryTopBar(
                                    currentUser = currentUser,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { viewModel.toggleDarkTheme() },
                                    onOpenAuth = { viewModel.toggleAuthDialog(true) },
                                    onOpenAdmin = { viewModel.toggleAdminPanel(true) },
                                    onSearchClick = { viewModel.selectTab(GalleryTab.EXPLORE) }
                                )
                            },
                            bottomBar = {
                                GalleryBottomNav(
                                    currentTab = currentTab,
                                    unreadNotificationsCount = unreadCount,
                                    onTabSelected = { viewModel.selectTab(it) }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    },
                                    label = "TabTransition"
                                ) { tab ->
                                    when (tab) {
                                        GalleryTab.HOME -> HomeScreen(viewModel = viewModel)
                                        GalleryTab.EXPLORE -> ExploreScreen(viewModel = viewModel)
                                        GalleryTab.CREATE -> CreateScreen(viewModel = viewModel)
                                        GalleryTab.MARKETPLACE -> MarketplaceScreen(viewModel = viewModel)
                                        GalleryTab.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                                        GalleryTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }

                    // Modal Dialogs & Sheets
                    inspectedPost?.let { post ->
                        ArtworkDetailDialog(
                            post = post,
                            viewModel = viewModel,
                            onDismiss = { viewModel.inspectPost(null) }
                        )
                    }

                    commentingPostId?.let { postId ->
                        CommentsBottomSheet(
                            postId = postId,
                            viewModel = viewModel,
                            onDismiss = { viewModel.openComments(null) }
                        )
                    }

                    purchasingItem?.let { pair ->
                        PurchaseBottomSheet(
                            itemPair = pair,
                            viewModel = viewModel,
                            onDismiss = { viewModel.closePurchase() }
                        )
                    }

                    if (showAuthDialog) {
                        AuthDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.toggleAuthDialog(false) }
                        )
                    }
                }
            }
        }
    }
}
