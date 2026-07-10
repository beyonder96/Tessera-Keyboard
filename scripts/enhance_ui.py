import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I want to add an elegant background decoration
bg_replacement = """                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0A0F14), // Deep space
                                        Color(0xFF001F22), // Deep teal
                                        Color(0xFF003838)  // Lighter teal at bottom
                                    )
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        // Decorative glowing orb in the background
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 100.dp, y = (-50).dp)
                                .size(250.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF06FBFB).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-80).dp, y = 100.dp)
                                .size(300.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF008B8B).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        StitchDashboardScreen(
                            modifier = Modifier.fillMaxSize()
                        )
"""
content = re.sub(r'                            \.background\(\n                                Brush\.verticalGradient\(\n                                    colors = listOf\(\n                                        Color\(0xFF001212\),\n                                        Color\(0xFF001C1C\),\n                                        Color\(0xFF002929\)\n                                    \)\n                                \)\n                            \)\n                            \.padding\(innerPadding\)\n                    \) \{\n                        StitchDashboardScreen\(\n                            modifier = Modifier\.fillMaxSize\(\)\n                        \)', bg_replacement, content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
