package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R

@Composable
fun ArtImageView(
    mediaUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    when {
        mediaUri.startsWith("res:") -> {
            val resName = mediaUri.removePrefix("res:")
            val resId = when (resName) {
                "art_solitude_dawn" -> R.drawable.art_solitude_dawn
                "art_charcoal_hands" -> R.drawable.art_charcoal_hands
                "art_watercolor_mist" -> R.drawable.art_watercolor_mist
                "market_field_kit" -> R.drawable.market_field_kit
                "avatar_elena" -> R.drawable.avatar_elena
                "ic_manukx_logo" -> R.drawable.ic_manukx_logo
                else -> R.drawable.ic_manukx_logo
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }

        mediaUri.startsWith("data:image/") -> {
            val bitmap = remember(mediaUri) {
                try {
                    val base64Data = mediaUri.substringAfter("base64,")
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                Box(
                    modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_manukx_logo),
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(0.5f)
                    )
                }
            }
        }

        mediaUri.isNotBlank() -> {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(mediaUri)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.art_charcoal_hands),
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            )
        }

        else -> {
            Image(
                painter = painterResource(id = R.drawable.avatar_elena),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
