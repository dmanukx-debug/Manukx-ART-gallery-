package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArtPostEntity
import com.example.data.model.UserEntity
import com.example.ui.components.ArtImageView
import com.example.ui.components.BadgeChip
import com.example.ui.components.EmptyGalleryState
import com.example.ui.theme.GalleryPrimary
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun ProfileScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()
    val savedIds by viewModel.savedPostIds.collectAsState()
    val userOrders by viewModel.userOrders.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Works, 1: Reels, 2: For Sale, 3: Saved, 4: Orders
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val userPosts = remember(allPosts, currentUser) {
        if (currentUser == null) emptyList()
        else allPosts.filter { it.authorId == currentUser!!.userId }
    }

    val userReels = remember(userPosts) {
        userPosts.filter { it.isReel }
    }

    val userOriginals = remember(userPosts) {
        userPosts.filter { it.isForSale }
    }

    val savedArtworks = remember(allPosts, savedIds) {
        allPosts.filter { savedIds.contains(it.id) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header Profile Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, GalleryPrimary, CircleShape)
                        ) {
                            ArtImageView(
                                mediaUri = currentUser?.avatarUrl?.ifBlank { "res:avatar_elena" } ?: "res:avatar_elena",
                                contentDescription = currentUser?.displayName,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Stats columns
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStatColumn(count = userPosts.size, label = "Works")
                            ProfileStatColumn(count = currentUser?.followersCount ?: 0, label = "Followers")
                            ProfileStatColumn(count = currentUser?.followingCount ?: 0, label = "Following")
                            ProfileStatColumn(count = userOrders.size, label = "Acquired")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name, Handle, Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentUser?.displayName ?: "Atelier Artist",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        BadgeChip(
                            text = currentUser?.badge ?: "Resident Artist",
                            color = GalleryPrimary
                        )
                    }

                    Text(
                        text = "@${currentUser?.username ?: "artist"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentUser?.specialty ?: "Fine Art Drawing & Painting",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GalleryPrimary
                    )

                    if (!currentUser?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentUser!!.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Edit Profile", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { viewModel.toggleAuthDialog(true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Switch / Log In")
                        }

                        if (currentUser?.role == "ADMIN") {
                            IconButton(
                                onClick = { viewModel.toggleAdminPanel(true) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = GalleryPrimary)
                            }
                        }
                    }
                }
            }

            // Tabs for user content
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = GalleryPrimary
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.GridOn, contentDescription = "Works") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.Videocam, contentDescription = "Reels") }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "For Sale") }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Saved") }
                    )
                    Tab(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        icon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = "Orders") }
                    )
                }
            }

            // Tab Content
            when (activeTab) {
                0 -> { // All Works
                    if (userPosts.isEmpty()) {
                        item {
                            EmptyGalleryState(
                                icon = Icons.Default.Palette,
                                title = "Your Atelier Gallery is Empty",
                                message = "You haven't cataloged any artworks yet. Publish your drawings, studies, or paintings to showcase your portfolio.",
                                actionLabel = "Publish Artwork",
                                onAction = { viewModel.selectTab(GalleryTab.CREATE) }
                            )
                        }
                    } else {
                        items(userPosts) { post ->
                            ArtworkProfileItemRow(post = post, onClick = { viewModel.inspectPost(post) })
                        }
                    }
                }
                1 -> { // Reels
                    if (userReels.isEmpty()) {
                        item {
                            EmptyGalleryState(
                                icon = Icons.Default.Videocam,
                                title = "No Process Reels",
                                message = "Share your drawing time-lapse videos and speedpaint process clips with the community.",
                                actionLabel = "Upload Process Reel",
                                onAction = { viewModel.selectTab(GalleryTab.CREATE) }
                            )
                        }
                    } else {
                        items(userReels) { post ->
                            ArtworkProfileItemRow(post = post, onClick = { viewModel.inspectPost(post) })
                        }
                    }
                }
                2 -> { // For Sale
                    if (userOriginals.isEmpty()) {
                        item {
                            EmptyGalleryState(
                                icon = Icons.Default.Storefront,
                                title = "No Works Listed for Sale",
                                message = "You haven't listed any original pieces in the Gallery Marketplace yet.",
                                actionLabel = "List an Original Work",
                                onAction = { viewModel.selectTab(GalleryTab.CREATE) }
                            )
                        }
                    } else {
                        items(userOriginals) { post ->
                            ArtworkProfileItemRow(post = post, onClick = { viewModel.inspectPost(post) })
                        }
                    }
                }
                3 -> { // Saved
                    if (savedArtworks.isEmpty()) {
                        item {
                            EmptyGalleryState(
                                icon = Icons.Outlined.BookmarkBorder,
                                title = "No Saved Artworks",
                                message = "Artworks and drawings you bookmark will be stored in your private atelier collection for inspiration.",
                                actionLabel = "Explore Masterworks",
                                onAction = { viewModel.selectTab(GalleryTab.EXPLORE) }
                            )
                        }
                    } else {
                        items(savedArtworks) { post ->
                            ArtworkProfileItemRow(post = post, onClick = { viewModel.inspectPost(post) })
                        }
                    }
                }
                4 -> { // Orders & Acquisitions
                    if (userOrders.isEmpty()) {
                        item {
                            EmptyGalleryState(
                                icon = Icons.Outlined.ShoppingBag,
                                title = "No Orders or Acquisitions",
                                message = "When you acquire artwork or art supplies from the Marketplace, your acquisition records will be listed here."
                            )
                        }
                    } else {
                        items(userOrders) { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))) {
                                        ArtImageView(mediaUri = order.itemImage, contentDescription = order.itemTitle, modifier = Modifier.fillMaxSize())
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = order.itemTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(text = "Order #${order.orderId} • Status: ${order.status}", style = MaterialTheme.typography.labelSmall, color = GalleryPrimary)
                                        Text(text = "Price: $${order.price.toInt()} USD", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            viewModel = viewModel,
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
private fun ProfileStatColumn(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ArtworkProfileItemRow(
    post: ArtPostEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                ArtImageView(mediaUri = post.mediaUri, contentDescription = post.title, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "${post.category} • ${post.medium}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (post.isForSale) {
                    Text(text = "For Sale: $${post.price.toInt()} USD", style = MaterialTheme.typography.labelSmall, color = GalleryPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "${post.likesCount}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
