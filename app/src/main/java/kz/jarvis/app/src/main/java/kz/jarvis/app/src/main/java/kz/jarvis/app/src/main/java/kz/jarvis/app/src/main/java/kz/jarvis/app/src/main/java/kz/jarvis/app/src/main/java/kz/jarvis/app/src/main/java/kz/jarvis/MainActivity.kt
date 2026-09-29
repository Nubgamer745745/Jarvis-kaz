package kz.jarvis

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guolindev.permissionx.PermissionX

class MainActivity : ComponentActivity() {

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        requestPerms()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00FFCC),
                    background = Color(0xFF0A0E27),
                    surface = Color(0xFF111633),
                    onPrimary = Color(0xFF0A0E27),
                    onBackground = Color(0xFF00FFCC),
                    onSurface = Color(0xFF00FFCC)
                )
            ) {
                MainScreen(
                    onStart = { startJarvis() },
                    onStop = { stopJarvis() },
                    onCommands = {
                        startActivity(Intent(this, CommandsActivity::class.java))
                    },
                    onSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onAccessibility = { openAccessibilitySettings() }
                )
            }
        }
    }

    private fun requestPerms() {
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            .request { allGranted, _, _ ->
                if (!allGranted) {
                    Toast.makeText(
                        this,
                        "Рұқсаттарсыз Jarvis жұмыс істемейді",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun startJarvis() {
        val i = Intent(this, JarvisService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(i)
        } else {
            startService(i)
        }
        Toast.makeText(this, "Jarvis іске қосылды", Toast.LENGTH_SHORT).show()
    }

    private fun stopJarvis() {
        stopService(Intent(this, JarvisService::class.java))
        Toast.makeText(this, "Jarvis тоқтатылды", Toast.LENGTH_SHORT).show()
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Exception) { e.printStackTrace() }
    }
}

@Composable
fun MainScreen(
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCommands: () -> Unit,
    onSettings: () -> Unit,
    onAccessibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Text(
            "JARVIS",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFCC)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Қазақ тілді дауыс көмекшісі",
            fontSize = 16.sp,
            color = Color(0xFF00FFCC).copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(40.dp))

        MenuBtn("▶  Jarvis іске қосу", Color(0xFF00FFCC), onStart)
        Spacer(Modifier.height(12.dp))

        MenuBtn("⏹  Тоқтату", Color(0xFFFF3366), onStop)
        Spacer(Modifier.height(12.dp))

        MenuBtn("📋  Командалар", Color(0xFF00FFCC), onCommands)
        Spacer(Modifier.height(12.dp))

        MenuBtn("🎯  Арнайы мүмкіндіктер", Color(0xFFFFCC00), onAccessibility)
        Spacer(Modifier.height(12.dp))

        MenuBtn("⚙  Параметрлер", Color(0xFF00FFCC), onSettings)

        Spacer(Modifier.height(40.dp))

        Text(
            "Jarvis v1.0 — @lowactiv & @unknoweed",
            fontSize = 12.sp,
            color = Color(0xFF00FFCC).copy(alpha = 0.4f)
        )
    }
}

@Composable
fun MenuBtn(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF111633),
            contentColor = color
        )
    ) {
        Text(text, fontSize = 18.sp)
    }
}
