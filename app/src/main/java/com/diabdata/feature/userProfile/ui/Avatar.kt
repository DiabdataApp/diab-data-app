package com.diabdata.feature.userProfile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import java.io.File

private val avatarColors = listOf(
    Color(0xFFE57373),
    Color(0xFFF06292),
    Color(0xFFBA68C8),
    Color(0xFF9575CD),
    Color(0xFF7986CB),
    Color(0xFF64B5F6),
    Color(0xFF4FC3F7),
    Color(0xFF4DD0E1),
    Color(0xFF4DB6AC),
    Color(0xFF81C784),
    Color(0xFFAED581),
    Color(0xFFFF8A65)
)

@Composable
fun UserAvatar(
    firstName: String?,
    lastName: String?,
    profilePhotoPath: String?,
    size: Dp = 35.dp,
    onClick: () -> Unit
) {
    val initial = remember(firstName, lastName) {
        when {
            !firstName.isNullOrBlank() -> firstName.first().uppercaseChar()
            !lastName.isNullOrBlank() -> lastName.first().uppercaseChar()
            else -> 'A'
        }
    }

    val backgroundColor = remember(initial) {
        avatarColors[initial.code % avatarColors.size]
    }

    val hasPhoto = remember(profilePhotoPath) {
        profilePhotoPath != null && File(profilePhotoPath).exists()
    }

    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .padding(5.dp)
            .clip(CircleShape)
            .then(
                if (!hasPhoto) {
                    Modifier.background(backgroundColor)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePhotoPath)
                        .memoryCacheKey(profilePhotoPath)
                        .diskCacheKey(profilePhotoPath)
                        .crossfade(true)
                        .size(Size.ORIGINAL)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
