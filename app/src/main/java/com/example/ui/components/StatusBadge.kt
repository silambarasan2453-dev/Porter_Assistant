package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RangeClassification
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGrayBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg

@Composable
fun StatusBadge(
    classification: RangeClassification,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val (bgColor, textColor, borderColor, icon) = when (classification) {
        RangeClassification.WITHIN_RANGE -> Quadruple(
            StatusGreenBg.copy(alpha = 0.5f),
            StatusGreen,
            StatusGreen.copy(alpha = 0.7f),
            Icons.Default.CheckCircle
        )
        RangeClassification.REVIEW -> Quadruple(
            StatusAmberBg.copy(alpha = 0.5f),
            StatusAmber,
            StatusAmber.copy(alpha = 0.7f),
            Icons.Default.WarningAmber
        )
        RangeClassification.OUTSIDE_RANGE -> Quadruple(
            StatusRedBg.copy(alpha = 0.5f),
            StatusRed,
            StatusRed.copy(alpha = 0.7f),
            Icons.Default.Warning
        )
        RangeClassification.UNKNOWN -> Quadruple(
            StatusGrayBg.copy(alpha = 0.5f),
            StatusGray,
            StatusGray.copy(alpha = 0.7f),
            Icons.Default.HelpOutline
        )
    }

    val shape = RoundedCornerShape(if (isLarge) 12.dp else 8.dp)
    val padH = if (isLarge) 16.dp else 10.dp
    val padV = if (isLarge) 8.dp else 4.dp
    val iconSize = if (isLarge) 20.dp else 14.dp
    val fontSize = if (isLarge) 16.sp else 12.sp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .testTag("status_badge_${classification.name.lowercase()}")
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = padH, vertical = padV)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = classification.label,
            tint = textColor,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = classification.label,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
