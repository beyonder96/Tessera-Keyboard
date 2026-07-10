import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Mic
"""

content = content.replace("import androidx.compose.ui.graphics.Brush", imports + "import androidx.compose.ui.graphics.Brush")

ui_add = """
        // Microphone permission
        var hasMicPermission by remember { 
            mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) 
        }
        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasMicPermission = granted
        }
        
        if (!hasMicPermission) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0017), contentColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.2.dp, Color.White)
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONCEDER PERMISSÃO DE MICROFONE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
"""

content = content.replace("// Theme Selection", ui_add + "\n        // Theme Selection")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
