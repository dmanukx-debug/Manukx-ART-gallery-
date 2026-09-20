package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.UserEntity
import com.example.ui.components.ArtImageView
import com.example.ui.components.BadgeChip
import com.example.ui.components.EmptyGalleryState
import com.example.ui.theme.GalleryPrimary
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun ExploreScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val allPosts by viewModel.allPosts.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var activeTypeFilter by remember { mutableStateOf("All") } // "All", "Originals", "Reels"

    // Search and category matching
    val filteredPosts = remember(allPosts, searchQuery, selectedCategory, activeTypeFilter) {
        allPosts.filter { post ->
            val matchesSearch = searchQuery.isBlank() ||
                post.title.contains(searchQuery, ignoreCase = true) ||
                post.description.contains(searchQuery, ignoreCase = true) ||
                post.authorName.contains(searchQuery, ignoreCase = true) ||
                post.category.contains(searchQuery, ignoreCase = true) ||
                post.medium.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" || post.category.equals(selectedCategory, ignoreCase = true)

            val matchesType = when (activeTypeFilter) {
                "Originals" -> post.isForSale && !post.isSold
                "Reels" -> post.isReel
                else -> true
            }

            matchesSearch && matchesCategory && matchesType
        }
    }

    val matchedUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allUsers.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.specialty.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search artists, artworks, reels, mediums...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = GalleryPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GalleryPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }

        // Horizontal Category Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "All",
                    onClick = { viewModel.setSelectedCategory("All") },
                    label = { Text("All Mediums", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GalleryPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            items(categories, key = { it.categoryId }) { cat ->
                FilterChip(
                    selected = selectedCategory == cat.name,
                    onClick = { viewModel.setSelectedCategory(cat.name) },
                    label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GalleryPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Type Filter Chips: All, Originals For Sale, Drawing Videos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Originals", "Reels").forEach { type ->
                SuggestionChip(
                    onClick = { activeTypeFilter = type },
                    label = {
                        Text(
                            text = if (type == "Reels") "Drawing Videos & Reels" else if (type == "Originals") "Originals For Sale" else "All Works",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (activeTypeFilter == type) GalleryPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (activeTypeFilter == type) GalleryPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Matched Artists row if searching
        if (matchedUsers.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ARTISTS & ATELIERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GalleryPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(matchedUsers, key = { it.userId }) { user ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { viewModel.selectTab(GalleryTab.PROFILE) }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                ) {
                                    ArtImageView(
                                        mediaUri = user.avatarUrl.ifBlank { "res:avatar_elena" },
                                        contentDescription = user.displayName,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column {
                                    Text(user.displayName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(user.specialty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Artwork Grid
        if (filteredPosts.isEmpty()) {
            EmptyGalleryState(
                icon = Icons.Default.Explore,
                title = "No Artworks Found",
                message = if (searchQuery.isNotBlank()) "No pieces match \"$searchQuery\" in this taxonomy." else "No artworks have been cataloged in this medium category yet.",
                actionLabel = "Publish Artwork",
                onAction = { viewModel.selectTab(GalleryTab.CREATE) }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    ExploreArtworkGridCard(
                        post = post,
                        onClick = { viewModel.inspectPost(post) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreArtworkGridCard(
    post: ArtPostEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                ArtImageView(
                    mediaUri = post.mediaUri,
                    contentDescription = post.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom gradient for legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 80f
                            )
                        )
                )

                // Reel Badge
                if (post.isReel) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text(
                                text = if (post.videoDurationSec > 0) "${post.videoDurationSec}s" else "Reel",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Price badge
                if (post.isForSale) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = GalleryPrimary
                    ) {
                        Text(
                            text = if (post.isSold) "SOLD" else "$${post.price.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Likes indicator
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${post.authorName} • ${post.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
