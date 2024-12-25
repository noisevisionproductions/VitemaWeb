package com.noisevisionsoftware.fitapplication.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClickableTextWithBackgroundForLoginAndRegister(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color.DarkGray,
    pressedBackgroundColor: Color = Color.Blue.copy(alpha = 0.2f),
    textColor: Color = Color.Blue,
    fontSize: Int = 14
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) pressedBackgroundColor else backgroundColor,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animatedColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            style = LocalTextStyle.current.copy(
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}
