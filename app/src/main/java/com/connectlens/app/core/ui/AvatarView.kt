package com.connectlens.app.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectlens.app.core.common.PhoneNumberUtils

/**
 * Circular avatar showing contact initials.
 * Colour is derived from the first character of the name for visual variety.
 */
@Composable
fun AvatarView(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val initials  = PhoneNumberUtils.initialsFor(name)
    val bgColor   = avatarColorFor(name)
    val textColor = if (bgColor.luminance() > 0.4f) Color.Black else Color.White

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            color      = textColor,
            fontSize   = (size.value * 0.38f).sp,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center
        )
    }
}

/**
 * Returns a deterministic background colour for a contact name.
 * Uses the hashCode to pick from a curated palette.
 */
private fun avatarColorFor(name: String): Color {
    val palette = listOf(
        Color(0xFF3949AB), // indigo
        Color(0xFF00897B), // teal
        Color(0xFF7B1FA2), // purple
        Color(0xFF1565C0), // blue
        Color(0xFF2E7D32), // green
        Color(0xFFC62828), // red
        Color(0xFF558B2F), // light green
        Color(0xFF6A1B9A), // deep purple
        Color(0xFF00695C), // dark teal
        Color(0xFF283593), // dark indigo
    )
    val index = Math.abs(name.hashCode()) % palette.size
    return palette[index]
}

/** Simple luminance approximation for contrast decision. */
private fun Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
