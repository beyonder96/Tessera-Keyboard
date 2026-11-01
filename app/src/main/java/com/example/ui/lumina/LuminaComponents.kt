package com.example.ui.lumina

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.LuminaTokens.ActiveSpecularRimBrush
import com.example.ui.lumina.LuminaTokens.CardSurfaceBrush
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaCyanGlow
import com.example.ui.lumina.LuminaTokens.ObsidianDeepShadow
import com.example.ui.lumina.LuminaTokens.ObsidianGlass
import com.example.ui.lumina.LuminaTokens.ObsidianGlassElevated
import com.example.ui.lumina.LuminaTokens.ObsidianGlassSubtle
import com.example.ui.lumina.LuminaTokens.SpecularRimBrush
import com.example.ui.lumina.LuminaTokens.TextDimTertiary
import com.example.ui.lumina.LuminaTokens.TextMutedSecondary
import com.example.ui.lumina.LuminaTokens.TextPureLight

/**
 * Cartão Monolith com Borda Especular Chanfrada (Double-Stroke Rim) e Profundidade Tátil.
 */
@Composable
fun MonolithSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "MonolithPressScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val rimBrush = if (isActive) ActiveSpecularRimBrush else SpecularRimBrush

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isActive) 12.dp else 6.dp,
                shape = shape,
                ambientColor = ObsidianDeepShadow,
                spotColor = if (isActive) LuminaCyanGlow else ObsidianDeepShadow
            )
            .clip(shape)
            .background(CardSurfaceBrush)
            .border(1.dp, rimBrush, shape)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            )
            .padding(18.dp)
    ) {
        content()
    }
}

/**
 * Interruptor Mecânico Tátil de Alta Precisão (Inspirado em Equipamento de Som & Horologia).
 */
@Composable
fun TactileSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val transitionProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SwitchProgress"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) ObsidianGlassElevated else ObsidianGlassSubtle,
        animationSpec = tween(durationMillis = 180),
        label = "TrackColor"
    )

    val ledColor by animateColorAsState(
        targetValue = if (checked) LuminaCyan else Color(0xFF475569),
        animationSpec = tween(durationMillis = 180),
        label = "LedColor"
    )

    Box(
        modifier = modifier
            .width(52.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .border(
                1.dp,
                if (checked) ActiveSpecularRimBrush else SpecularRimBrush,
                RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Trilho do Polegar
        val travelDistance = 24.dp
        Box(
            modifier = Modifier
                .offset(x = travelDistance * transitionProgress)
                .size(22.dp)
                .shadow(4.dp, CircleShape, spotColor = if (checked) LuminaCyanGlow else ObsidianDeepShadow)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (checked) Color(0xFF26334A) else Color(0xFF232833),
                            if (checked) Color(0xFF141C2A) else Color(0xFF171A21)
                        )
                    )
                )
                .border(
                    1.dp,
                    if (checked) LuminaCyan.copy(alpha = 0.6f) else Color(0x33FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Micro-LED de Status
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(ledColor)
                    .then(
                        if (checked) {
                            Modifier.drawWithCache {
                                onDrawBehind {
                                    drawCircle(
                                        color = LuminaCyanGlow,
                                        radius = size.width * 1.8f
                                    )
                                }
                            }
                        } else Modifier
                    )
            )
        }
    }
}

/**
 * Item de Configuração com Layout Editorial e Interruptor Tátil Integrado.
 */
@Composable
fun LuminaSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPureLight
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedSecondary,
                lineHeight = 17.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        TactileSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Cabeçalho de Seção Editorial com Tag de Categoria e Linha Guia.
 */
@Composable
fun LuminaSectionTitle(
    tag: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = tag.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Bold
            ),
            color = LuminaCyan
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            ),
            color = TextPureLight
        )
    }
}
