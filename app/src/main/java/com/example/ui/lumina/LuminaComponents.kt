package com.example.ui.lumina

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.LuminaTokens.ActiveSpecularRimBrush
import com.example.ui.lumina.LuminaTokens.CardSurfaceBrush
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaCyanGlow
import com.example.ui.lumina.LuminaTokens.ObsidianDeepShadow
import com.example.ui.lumina.LuminaTokens.ObsidianGlassElevated
import com.example.ui.lumina.LuminaTokens.ObsidianGlassSubtle
import com.example.ui.lumina.LuminaTokens.SpecularRimBrush
import com.example.ui.lumina.LuminaTokens.TesseraCardBg
import com.example.ui.lumina.LuminaTokens.TesseraCardBorder
import com.example.ui.lumina.LuminaTokens.TesseraDivider
import com.example.ui.lumina.LuminaTokens.TesseraIconCircleBg
import com.example.ui.lumina.LuminaTokens.TesseraPillActiveBg
import com.example.ui.lumina.LuminaTokens.TesseraPillActiveBorder
import com.example.ui.lumina.LuminaTokens.TesseraPillTrackBg
import com.example.ui.lumina.LuminaTokens.TextMutedSecondary
import com.example.ui.lumina.LuminaTokens.TextPureLight

/**
 * Cartão Monolith / Tessera Core com Borda Especular e Profundidade Tátil.
 */
@Composable
fun MonolithSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
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
                elevation = if (isActive) 10.dp else 4.dp,
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
 * Cabeçalho de categoria no padrão Tessera Core (ex: "TELA & EXPERIÊNCIA").
 */
@Composable
fun TesseraSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp
        ),
        color = TextMutedSecondary,
        modifier = modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

/**
 * Cartão agrupado de configurações no padrão Tessera Core (RoundedCornerShape(26.dp), borda #262630).
 */
@Composable
fun TesseraGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = TesseraCardBg,
        border = BorderStroke(1.dp, TesseraCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Divisor horizontal sutil do Tessera Core entre linhas de um mesmo cartão.
 */
@Composable
fun TesseraItemDivider(withIconOffset: Boolean = false) {
    HorizontalDivider(
        modifier = Modifier.padding(
            start = if (withIconOffset) 72.dp else 18.dp,
            end = 18.dp
        ),
        thickness = 0.5.dp,
        color = TesseraDivider
    )
}

/**
 * Linha de configuração Tessera Core com ícone circular (40.dp) e interruptor tátil.
 */
@Composable
fun TesseraSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = TextPureLight
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0xFF282834) else TesseraIconCircleBg)
                .border(
                    0.5.dp,
                    if (checked) Color(0xFF3A3A48) else Color(0x18FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (checked) TextPureLight else iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = TextPureLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 16.sp),
                fontWeight = FontWeight.Normal,
                color = TextMutedSecondary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        TactileSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Controle Segmentado em Pílula Deslizante no padrão Tessera Core.
 */
@Composable
fun TesseraSegmentedControl(
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TesseraPillTrackBg)
            .border(1.dp, TesseraCardBorder, RoundedCornerShape(14.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selectedValue
            val bg by animateColorAsState(
                targetValue = if (isSelected) TesseraPillActiveBg else Color.Transparent,
                animationSpec = tween(180),
                label = "SegmentBg_$value"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextPureLight else TextMutedSecondary,
                animationSpec = tween(180),
                label = "SegmentText_$value"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(bg)
                    .then(
                        if (isSelected) {
                            Modifier.border(1.dp, TesseraPillActiveBorder, RoundedCornerShape(11.dp))
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onValueSelected(value) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Interruptor Mecânico Tátil de Alta Precisão (Tessera Core Switch).
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
        targetValue = if (checked) Color(0xFF282834) else ObsidianGlassSubtle,
        animationSpec = tween(durationMillis = 180),
        label = "TrackColor"
    )

    val ledColor by animateColorAsState(
        targetValue = if (checked) Color(0xFFF5F5F7) else Color(0xFF52525E),
        animationSpec = tween(durationMillis = 180),
        label = "LedColor"
    )

    Box(
        modifier = modifier
            .width(50.dp)
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
        val travelDistance = 22.dp
        Box(
            modifier = Modifier
                .offset(x = travelDistance * transitionProgress)
                .size(22.dp)
                .shadow(4.dp, CircleShape, spotColor = if (checked) LuminaCyanGlow else ObsidianDeepShadow)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (checked) Color(0xFF2B384E) else Color(0xFF262630),
                            if (checked) Color(0xFF192232) else Color(0xFF1A1A22)
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPureLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = TextMutedSecondary,
                lineHeight = 16.sp
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
