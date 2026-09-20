package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArtPostEntity
import com.example.ui.components.*
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GalleryPrimary
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun HomeScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val allPosts by viewModel.allPosts.collectAsState()
    val likedIds by viewModel.likedPostIds.collectAsState()
    val savedIds by viewModel.savedPostIds.collectAsState()
    val announcements by viewModel.activeAnnouncements.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeSubFilter by remember { mutableStateOf("For You") } // "For You", "Originals", "Time-lapses"

    val filteredPosts = remember(allPosts, activeSubFilter) {
        when (activeSubFilter) {
            "Originals" -> allPosts.filter { it.isForSale && !it.isSold }
            "Time-lapses" -> allPosts.filter { it.isReel }
            else -> allPosts
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Studio Reels & Workshops Stories Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Studio Reels & Workshops",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Upload Reel",
                        style = MaterialTheme.typography.labelSmall,
                        color = GalleryPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.selectTab(GalleryTab.CREATE) }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Current User's Add Story
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.selectTab(GalleryTab.CREATE) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Share Study",
                                    tint = GalleryPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Share Study",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Master Artist Studio Stories
                    item {
                        StoryAvatarItem(
                            title = "Elena Rostova",
                            subtitle = "LIVE Charcoal",
                            imageUri = "res:avatar_elena",
                            hasStory = true,
                            onClick = {
                                if (allPosts.isNotEmpty()) {
                                    viewModel.inspectPost(allPosts.firstOrNull())
                                }
                            }
                        )
                    }

                    item {
                        StoryAvatarItem(
                            title = "Marcus Aurel",
                            subtitle = "Pencil Tonal",
                            imageUri = "res:art_charcoal_hands",
                            hasStory = true,
                            onClick = {
                                if (allPosts.isNotEmpty()) {
                                    viewModel.inspectPost(allPosts.firstOrNull())
                                }
                            }
                        )
                    }

                    item {
                        StoryAvatarItem(
                            title = "Botanical Studio",
                            subtitle = "Glazing W.I.P",
                            imageUri = "res:art_watercolor_mist",
                            hasStory = false,
                            onClick = {}
                        )
                    }
                }
            }
        }

        // Active Official Announcement Banner
        if (announcements.isNotEmpty()) {
            val announcement = announcements.first()
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GalleryPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = GalleryPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = announcement.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = announcement.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Sub-filter tabs (Following, For You, Originals, Time-lapses)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("For You", "Originals", "Time-lapses").forEach { filter ->
                    FilterChip(
                        selected = activeSubFilter == filter,
                        onClick = { activeSubFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GalleryPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Posts List or Empty State
        if (filteredPosts.isEmpty()) {
            item {
                EmptyGalleryState(
                    icon = Icons.Default.Palette,
                    title = "Atelier Canvas is Open",
                    message = "No real art posts submitted yet under this filter. Tap Publish to upload a sketch, time-lapse reel, or original painting!",
                    actionLabel = "Publish Real Artwork",
                    onAction = { viewModel.selectTab(GalleryTab.CREATE) }
                )
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                ArtPostCard(
                    post = post,
                    isLiked = likedIds.contains(post.id),
                    isSaved = savedIds.contains(post.id),
                    onLikeToggle = { viewModel.toggleLike(post.id) },
                    onSaveToggle = { viewModel.toggleSave(post.id) },
                    onCommentClick = { viewModel.openComments(post.id) },
                    onInspectClick = { viewModel.inspectPost(post) },
                    onInquireClick = { viewModel.openPurchase("ARTWORK", post) },
                    onAuthorClick = { viewModel.selectTab(GalleryTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun StoryAvatarItem(
    title: String,
    subtitle: String,
    imageUri: String,
    hasStory: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .border(
                    width = if (hasStory) 2.dp else 1.dp,
                    brush = if (hasStory) Brush.sweepGradient(listOf(GalleryPrimary, Color(0xFFF59E0B), GalleryPrimary)) else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)),
                    shape = CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
        ) {
            ArtImageView(
                mediaUri = imageUri,
                contentDescription = title,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
