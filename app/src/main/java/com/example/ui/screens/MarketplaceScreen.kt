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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArtPostEntity
import com.example.data.model.MarketplaceProductEntity
import com.example.ui.components.ArtImageView
import com.example.ui.components.BadgeChip
import com.example.ui.components.EmptyGalleryState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GalleryPrimary
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun MarketplaceScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.marketplaceTab.collectAsState()
    val originalsForSale by viewModel.originalsForSale.collectAsState()
    val products by viewModel.marketplaceProducts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSupplyCategory by remember { mutableStateOf("All") }

    val supplyCategories = listOf("All", "Brushes", "Paints & Pigments", "Paper & Canvas", "Pencils & Charcoal", "Studio Kits")

    val filteredOriginals = remember(originalsForSale, searchQuery) {
        if (searchQuery.isBlank()) originalsForSale
        else originalsForSale.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.authorName.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.medium.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredProducts = remember(products, searchQuery, selectedSupplyCategory) {
        products.filter { prod ->
            val matchesSearch = searchQuery.isBlank() ||
                prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.description.contains(searchQuery, ignoreCase = true) ||
                prod.category.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedSupplyCategory == "All" || prod.category.equals(selectedSupplyCategory, ignoreCase = true)
            matchesSearch && matchesCat
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Bar (Original Artworks vs Art Materials Market)
        TabRow(
            selectedTabIndex = if (activeSubTab == "ARTWORKS") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GalleryPrimary
        ) {
            Tab(
                selected = activeSubTab == "ARTWORKS",
                onClick = { viewModel.setMarketplaceTab("ARTWORKS") },
                text = {
                    Text(
                        "Original Artworks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (activeSubTab == "ARTWORKS") FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.Palette, contentDescription = null) }
            )
            Tab(
                selected = activeSubTab == "MATERIALS",
                onClick = { viewModel.setMarketplaceTab("MATERIALS") },
                text = {
                    Text(
                        "Art Materials & Supplies",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (activeSubTab == "MATERIALS") FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(Icons.Default.Brush, contentDescription = null) }
            )
        }

        // Search Bar & List item action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (activeSubTab == "ARTWORKS") "Search original paintings..." else "Search supplies & pigments...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GalleryPrimary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = { viewModel.selectTab(GalleryTab.CREATE) },
                colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sell", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        // Secondary category filter if Materials tab
        if (activeSubTab == "MATERIALS") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(supplyCategories) { cat ->
                    FilterChip(
                        selected = selectedSupplyCategory == cat,
                        onClick = { selectedSupplyCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GalleryPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Content
        if (activeSubTab == "ARTWORKS") {
            if (filteredOriginals.isEmpty()) {
                EmptyGalleryState(
                    icon = Icons.Default.Storefront,
                    title = "No Originals Listed for Sale",
                    message = "No artist has listed an original painting or drawing under this query. You can list your original artwork in the Gallery Marketplace!",
                    actionLabel = "List Your Artwork for Sale",
                    onAction = { viewModel.selectTab(GalleryTab.CREATE) }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOriginals, key = { it.id }) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.inspectPost(post) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                ) {
                                    ArtImageView(
                                        mediaUri = post.mediaUri,
                                        contentDescription = post.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = GalleryPrimary
                                    ) {
                                        Text(
                                            text = "$${post.price.toInt()} USD",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "By ${post.authorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = post.medium,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GalleryPrimary,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.openPurchase("ARTWORK", post) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Acquire", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Art Materials Market
            if (filteredProducts.isEmpty()) {
                EmptyGalleryState(
                    icon = Icons.Default.Brush,
                    title = "Art Supplies Market is Open",
                    message = "No studio supplies or materials listed yet under this filter. Clean out your studio or sell your handmade pigments, sable brushes, and canvas kits!",
                    actionLabel = "List Art Materials for Sale",
                    onAction = { viewModel.selectTab(GalleryTab.CREATE) }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.productId }) { prod ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                ) {
                                    ArtImageView(
                                        mediaUri = prod.imageUri.ifBlank { "res:market_field_kit" },
                                        contentDescription = prod.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = GalleryPrimary
                                    ) {
                                        Text(
                                            text = "$${prod.price.toInt()} USD",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Black.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = prod.condition,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = prod.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Seller: ${prod.sellerName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.openPurchase("MATERIAL", prod) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Order Now", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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
