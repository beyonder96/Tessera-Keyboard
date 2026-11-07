package com.example.ui.lumina

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.LuminaTokens.DockGlassBrush
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaCyanGlow
import com.example.ui.lumina.LuminaTokens.ObsidianDeepShadow
import com.example.ui.lumina.LuminaTokens.SpecularRimBrush
import com.example.ui.lumina.LuminaTokens.TextDimTertiary
import com.example.ui.lumina.LuminaTokens.TextMutedSecondary
import com.example.ui.lumina.LuminaTokens.TextPureLight

data class DockTabItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)

/**
 * Dock Flutuante "Kinetic Capsule" em Vidro Fumê com Física de Mola e Indicador Magnético.
 */
@Composable
fun KineticCapsuleDock(
    tabs: List<DockTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val capsuleShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Cápsula Flutuante Principal
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = capsuleShape,
                    ambientColor = ObsidianDeepShadow,
                    spotColor = LuminaCyanGlow
                )
                .clip(capsuleShape)
                .background(DockGlassBrush)
                .border(1.dp, SpecularRimBrush, capsuleShape)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.96f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab.index == selectedIndex

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 0.95f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "DockIconScale_${tab.index}"
                    )

                    val pillAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "DockPillAlpha_${tab.index}"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) TextPureLight else TextMutedSecondary,
                        animationSpec = tween(durationMillis = 200),
                        label = "DockContentColor_${tab.index}"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) LuminaCyan else TextMutedSecondary,
                        animationSpec = tween(durationMillis = 200),
                        label = "DockIconColor_${tab.index}"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(LuminaTokens.TesseraPillActiveBg)
                                        .border(
                                            1.dp,
                                            LuminaTokens.TesseraPillActiveBorder,
                                            RoundedCornerShape(22.dp)
                                        )
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onTabSelected(tab.index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
