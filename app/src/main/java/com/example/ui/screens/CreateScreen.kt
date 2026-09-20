package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.ArtImageView
import com.example.ui.components.DrawingCanvas
import com.example.ui.theme.GalleryPrimary
import com.example.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    var publicationMode by remember { mutableStateOf("ARTWORK") } // "ARTWORK", "REEL", "MATERIAL"
    var mediaSourceMode by remember { mutableStateOf("CANVAS") } // "CANVAS", "PICKER", "PRESETS"

    // Artwork & Reel fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Pencil Drawing") }
    var medium by remember { mutableStateOf("Graphite & Charcoal") }
    var technique by remember { mutableStateOf("Cross-hatching, Blending") }
    var dimensions by remember { mutableStateOf("16 x 20 in") }
    var materialsUsed by remember { mutableStateOf("Fabriano 300g, Faber-Castell 9000") }
    var isForSale by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("250") }
    var hasCoA by remember { mutableStateOf(true) }
    var videoDurationSec by remember { mutableIntStateOf(30) }

    // Art Material fields
    var materialName by remember { mutableStateOf("") }
    var materialCategory by remember { mutableStateOf("Studio Kits") }
    var materialCondition by remember { mutableStateOf("Brand New") }
    var materialPriceText by remember { mutableStateOf("65") }
    var materialDescription by remember { mutableStateOf("") }

    // Selected image URI
    var mediaUri by remember { mutableStateOf("res:art_solitude_dawn") }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            mediaUri = uri.toString()
        }
    }

    val categories = listOf(
        "Portrait", "Pencil Drawing", "Watercolor", "Acrylic Art",
        "Digital Art", "Crafting", "Colored Pencil", "Anime", "Realistic Drawing"
    )

    val materialCategories = listOf(
        "Brushes", "Paints & Pigments", "Paper & Canvas", "Pencils & Charcoal", "Studio Kits"
    )

    val presets = listOf(
        Triple("Solitude at Dawn", "res:art_solitude_dawn", "Acrylic Art"),
        Triple("Hands Study", "res:art_charcoal_hands", "Pencil Drawing"),
        Triple("Fluid Mountain Mist", "res:art_watercolor_mist", "Watercolor"),
        Triple("Plein Air Field Kit", "res:market_field_kit", "Crafting")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector (Artwork Post, Drawing Reel, Art Material)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Triple("ARTWORK", "Art Post", Icons.Default.Palette),
                Triple("REEL", "Process Reel", Icons.Default.Videocam),
                Triple("MATERIAL", "Art Material", Icons.Default.Storefront)
            ).forEach { (mode, label, icon) ->
                val isSelected = publicationMode == mode
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) GalleryPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { publicationMode = mode }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Media Source Switcher
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "STUDIO MEDIA SOURCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GalleryPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mediaSourceMode == "CANVAS",
                    onClick = { mediaSourceMode = "CANVAS" },
                    label = { Text("Interactive Sketchpad") },
                    leadingIcon = { Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = mediaSourceMode == "PICKER",
                    onClick = {
                        mediaSourceMode = "PICKER"
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    label = { Text("Device Gallery") },
                    leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = mediaSourceMode == "PRESETS",
                    onClick = { mediaSourceMode = "PRESETS" },
                    label = { Text("Atelier Presets") },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Active Media Selection Area
        when (mediaSourceMode) {
            "CANVAS" -> {
                DrawingCanvas(
                    onExportBitmapDataUrl = { dataUrl ->
                        mediaUri = dataUrl
                    }
                )
            }
            "PRESETS" -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        val isSelected = mediaUri == preset.second
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    mediaUri = preset.second
                                    title = preset.first
                                    selectedCategory = preset.third
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) GalleryPrimary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                ArtImageView(mediaUri = preset.second, contentDescription = preset.first, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = preset.first, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
            "PICKER" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))) {
                        ArtImageView(mediaUri = mediaUri, contentDescription = "Selected media", modifier = Modifier.fillMaxSize())
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Photo Selected from Device", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change Photo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Preview of attached media
        if (mediaSourceMode == "CANVAS" && mediaUri.startsWith("data:image")) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(6.dp))) {
                        ArtImageView(mediaUri = mediaUri, contentDescription = "Drawn Artwork", modifier = Modifier.fillMaxSize())
                    }
                    Text("Your Sketch is Attached & Ready to Publish!", style = MaterialTheme.typography.labelMedium, color = GalleryPrimary)
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        if (publicationMode == "MATERIAL") {
            // Material form
            OutlinedTextField(
                value = materialName,
                onValueChange = { materialName = it },
                label = { Text("Material Name (e.g. Handmade Walnut Watercolor Box)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Category selector
            Text("Material Category", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(materialCategories) { cat ->
                    FilterChip(
                        selected = materialCategory == cat,
                        onClick = { materialCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = materialPriceText,
                    onValueChange = { materialPriceText = it },
                    label = { Text("Price (USD)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = materialCondition,
                    onValueChange = { materialCondition = it },
                    label = { Text("Condition") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = materialDescription,
                onValueChange = { materialDescription = it },
                label = { Text("Description & Specifications") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = {
                    val price = materialPriceText.toDoubleOrNull() ?: 0.0
                    if (materialName.isNotBlank()) {
                        viewModel.submitProduct(
                            name = materialName,
                            description = materialDescription,
                            imageUri = mediaUri,
                            category = materialCategory,
                            price = price,
                            condition = materialCondition,
                            onSuccess = {}
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("List Art Supplies on Marketplace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

        } else {
            // Artwork / Reel Form
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Artwork Title *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Artist Statement & Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Category Selection
            Text("Taxonomy Category", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Medium & Technique
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = medium,
                    onValueChange = { medium = it },
                    label = { Text("Medium (e.g. Charcoal)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = technique,
                    onValueChange = { technique = it },
                    label = { Text("Technique (e.g. Hatching)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Dimensions & Materials
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = dimensions,
                    onValueChange = { dimensions = it },
                    label = { Text("Dimensions (e.g. 18x24 in)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = materialsUsed,
                    onValueChange = { materialsUsed = it },
                    label = { Text("Materials (Paper, Pigment)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Reel Duration if REEL mode
            if (publicationMode == "REEL") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text("Time-lapse Video Duration: ${videoDurationSec}s", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = videoDurationSec.toFloat(),
                        onValueChange = { videoDurationSec = it.toInt() },
                        valueRange = 10f..120f
                    )
                }
            }

            // Commercial Marketplace Listing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("List Original Artwork for Sale", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Make available for collectors in the Gallery Marketplace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isForSale,
                            onCheckedChange = { isForSale = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GalleryPrimary)
                        )
                    }

                    if (isForSale) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Collector Price (USD)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = hasCoA,
                                onCheckedChange = { hasCoA = it },
                                colors = CheckboxDefaults.colors(checkedColor = GalleryPrimary)
                            )
                            Text("Include Physical Certificate of Authenticity (CoA)", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Publish Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val price = if (isForSale) priceText.toDoubleOrNull() ?: 0.0 else 0.0
                        viewModel.submitPost(
                            title = title,
                            description = description,
                            mediaUri = mediaUri,
                            mediaType = if (publicationMode == "REEL") "VIDEO_REEL" else "IMAGE",
                            category = selectedCategory,
                            medium = medium,
                            technique = technique,
                            dimensions = dimensions,
                            materialsUsed = materialsUsed,
                            isReel = publicationMode == "REEL",
                            videoDurationSec = if (publicationMode == "REEL") videoDurationSec else 0,
                            isForSale = isForSale,
                            price = price,
                            hasCertificateOfAuthenticity = hasCoA,
                            onSuccess = {
                                title = ""
                                description = ""
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GalleryPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Publish, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (publicationMode == "REEL") "Publish Process Reel" else "Publish to Atelier Gallery",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
