package com.example.ui.lumina

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaCyanGlow
import com.example.ui.lumina.LuminaTokens.ObsidianDeepShadow
import com.example.ui.lumina.LuminaTokens.TextDimTertiary
import com.example.ui.lumina.LuminaTokens.TextMutedSecondary
import com.example.ui.lumina.LuminaTokens.TextPureLight

/**
 * Visualizador Hero 3D Isometric Keycap com Dinâmica de Luz Especular e Inclinação Tátil.
 */
@Composable
fun MonolithKeycapVisualizer(
    modifier: Modifier = Modifier,
    onKeycapClick: (() -> Unit)? = null
) {
    var rawDragX by remember { mutableFloatStateOf(0f) }
    var rawDragY by remember { mutableFloatStateOf(0f) }

    val rotX by animateFloatAsState(
        targetValue = (-rawDragY / 15f).coerceIn(-18f, 18f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "RotX"
    )

    val rotY by animateFloatAsState(
        targetValue = (rawDragX / 15f).coerceIn(-22f, 22f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "RotY"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tag Tecnológica
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x1A38BDF8))
                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(LuminaCyan)
            )
            Text(
                text = "120HZ TACTILE MONOLITH",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.4.sp,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = LuminaCyan
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Keycap 3D Card com Especular e Interação
        Box(
            modifier = Modifier
                .size(108.dp)
                .graphicsLayer {
                    rotationX = rotX
                    rotationY = rotY
                    cameraDistance = 14f * density
                }
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = ObsidianDeepShadow,
                    spotColor = LuminaCyanGlow
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF252C3D),
                            Color(0xFF141924),
                            Color(0xFF0A0D14)
                        ),
                        center = Offset(54f + rotY * 2f, 54f - rotX * 2f),
                        radius = 120f
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x80FFFFFF),
                            Color(0x3338BDF8),
                            Color(0x10000000)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(108f, 108f)
                    ),
                    RoundedCornerShape(26.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            rawDragX += dragAmount.x
                            rawDragY += dragAmount.y
                        },
                        onDragEnd = {
                            rawDragX = 0f
                            rawDragY = 0f
                        },
                        onDragCancel = {
                            rawDragX = 0f
                            rawDragY = 0f
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Emblema Central
            Image(
                painter = painterResource(id = R.drawable.ic_tessera_logo),
                contentDescription = "Tessera Monolith",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TESSERA",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            ),
            color = TextPureLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Engine 120Hz • Precisão Voronoi • Síntese Acústica",
            style = MaterialTheme.typography.bodySmall,
            color = TextMutedSecondary
        )
    }
}
