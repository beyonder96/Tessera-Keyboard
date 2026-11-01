package com.example.ui.lumina

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.LuminaTokens.LuminaAmber
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaCyanGlow
import com.example.ui.lumina.LuminaTokens.LuminaEmerald
import com.example.ui.lumina.LuminaTokens.ObsidianGlassSubtle
import com.example.ui.lumina.LuminaTokens.TextDimTertiary
import com.example.ui.lumina.LuminaTokens.TextMutedSecondary
import com.example.ui.lumina.LuminaTokens.TextPureLight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tacômetro Radial de Alta Precisão (240°) para Telemetria Instantânea de WPM e Acurácia.
 */
@Composable
fun WpmTachometerView(
    wpm: Int,
    accuracy: Int,
    cpm: Int,
    modifier: Modifier = Modifier
) {
    val animatedWpm by animateFloatAsState(
        targetValue = wpm.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "TachometerWpm"
    )

    val maxWpm = 160f
    val sweepFraction = (animatedWpm / maxWpm).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrador Circular do Tacômetro
        Box(
            modifier = Modifier
                .size(200.dp)
                .drawWithCache {
                    val strokeWidth = 12.dp.toPx()
                    val arcSize = size.width - strokeWidth * 2
                    val topLeft = Offset(strokeWidth, strokeWidth)
                    val startAngle = 150f
                    val totalSweep = 240f

                    val backgroundTrackColor = Color(0x1F38BDF8)
                    val activeSweepAngle = totalSweep * sweepFraction

                    val gradientBrush = Brush.sweepGradient(
                        0.0f to LuminaCyan,
                        0.4f to LuminaEmerald,
                        0.8f to LuminaAmber,
                        1.0f to LuminaCyan,
                        center = Offset(size.width / 2, size.height / 2)
                    )

                    onDrawBehind {
                        // 1. Trilha de Fundo
                        drawArc(
                            color = backgroundTrackColor,
                            startAngle = startAngle,
                            sweepAngle = totalSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 2. Arco Ativo do WPM
                        if (activeSweepAngle > 1f) {
                            drawArc(
                                brush = gradientBrush,
                                startAngle = startAngle,
                                sweepAngle = activeSweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // 3. Agulha Bioluminescente
                            val currentAngleRad = Math.toRadians((startAngle + activeSweepAngle).toDouble())
                            val radius = arcSize / 2f
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            val tipX = centerX + (radius * cos(currentAngleRad)).toFloat()
                            val tipY = centerY + (radius * sin(currentAngleRad)).toFloat()

                            drawCircle(
                                color = LuminaCyanGlow,
                                radius = strokeWidth * 1.4f,
                                center = Offset(tipX, tipY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = strokeWidth * 0.45f,
                                center = Offset(tipX, tipY)
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedWpm.toInt()}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        fontSize = 44.sp
                    ),
                    color = TextPureLight
                )
                Text(
                    text = "PALAVRAS / MIN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.6.sp,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = LuminaCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Régua de Telemetria Secundária: Precisão & CPM
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Precisão
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianGlassSubtle)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$accuracy%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (accuracy >= 92) LuminaEmerald else LuminaAmber
                    )
                    Text(
                        text = "PRECISÃO",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextDimTertiary
                    )
                }
            }

            // Card CPM
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianGlassSubtle)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$cpm",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPureLight
                    )
                    Text(
                        text = "CARACTERES/MIN",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextDimTertiary
                    )
                }
            }
        }
    }
}
