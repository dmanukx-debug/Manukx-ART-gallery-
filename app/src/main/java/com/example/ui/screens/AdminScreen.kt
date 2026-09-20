package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.AnnouncementEntity
import com.example.data.model.CategoryEntity
import com.example.ui.components.ArtImageView
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GalleryPrimary
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: GalleryViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableIntStateOf(0) } // 0: Curators' Picks, 1: Categories, 2: Announcements, 3: Orders

    val allPosts by viewModel.allPosts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryDesc by remember { mutableStateOf("") }

    var newAnnounceTitle by remember { mutableStateOf("") }
    var newAnnounceMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MANUKX CURATORIAL CONSOLE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = adminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GalleryPrimary
            ) {
                Tab(selected = adminTab == 0, onClick = { adminTab = 0 }, text = { Text("Curate Art", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = adminTab == 1, onClick = { adminTab = 1 }, text = { Text("Categories", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = adminTab == 2, onClick = { adminTab = 2 }, text = { Text("Bulletins", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = adminTab == 3, onClick = { adminTab = 3 }, text = { Text("Orders", style = MaterialTheme.typography.labelSmall) })
            }

            when (adminTab) {
                0 -> { // Artworks curation
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Artwork Moderation & Curators' Pick Selection",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (allPosts.isEmpty()) {
                            item {
                                Text(
                                    text = "No artworks uploaded to the gallery yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(allPosts, key = { it.id }) { post ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))) {
                                            ArtImageView(mediaUri = post.mediaUri, contentDescription = post.title, modifier = Modifier.fillMaxSize())
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = post.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(text = "Artist: ${post.authorName} • Cat: ${post.category}", style = MaterialTheme.typography.bodySmall)
                                            if (post.isFeatured) {
                                                Text(text = "Curators' Pick", color = AccentGold, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        IconButton(onClick = { viewModel.adminToggleFeatured(post.id, !post.isFeatured) }) {
                                            Icon(
                                                imageVector = if (post.isFeatured) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "Feature",
                                                tint = AccentGold
                                            )
                                        }

                                        IconButton(onClick = { viewModel.adminDeletePost(post.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> { // Taxonomy Categories
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Art Taxonomy Management", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    label = { Text("New Category") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (newCategoryName.isNotBlank()) {
                                            viewModel.adminAddCategory(newCategoryName, newCategoryDesc, "palette")
                                            newCategoryName = ""
                                            newCategoryDesc = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary)
                                ) {
                                    Text("Add")
                                }
                            }
                        }

                        items(categories, key = { it.categoryId }) { cat ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = cat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        if (cat.description.isNotBlank()) {
                                            Text(text = cat.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.adminDeleteCategory(cat.categoryId) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> { // Announcements / Bulletins
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Broadcast Curatorial Bulletin", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newAnnounceTitle,
                                onValueChange = { newAnnounceTitle = it },
                                label = { Text("Bulletin Headline") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = newAnnounceMessage,
                                onValueChange = { newAnnounceMessage = it },
                                label = { Text("Announcement Content") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newAnnounceTitle.isNotBlank() && newAnnounceMessage.isNotBlank()) {
                                        viewModel.adminCreateAnnouncement(newAnnounceTitle, newAnnounceMessage)
                                        newAnnounceTitle = ""
                                        newAnnounceMessage = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary)
                            ) {
                                Text("Broadcast to Community")
                            }
                        }

                        items(allAnnouncements, key = { it.id }) { ann ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = ann.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(text = ann.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.adminDeleteAnnouncement(ann.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> { // Orders & Collector Registry
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text("Marketplace Acquisitions & Shipping", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        if (allOrders.isEmpty()) {
                            item {
                                Text("No acquisitions recorded yet in the gallery registry.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(allOrders, key = { it.orderId }) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Order #${order.orderId}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(text = "$${order.price.toInt()} USD", color = GalleryPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Text(text = "Item: ${order.itemTitle} (${order.itemType})", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Buyer: ${order.buyerName} • Seller: ${order.sellerName}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Ship to: ${order.shippingAddress}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Status: ${order.status}", style = MaterialTheme.typography.labelSmall, color = GalleryPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
