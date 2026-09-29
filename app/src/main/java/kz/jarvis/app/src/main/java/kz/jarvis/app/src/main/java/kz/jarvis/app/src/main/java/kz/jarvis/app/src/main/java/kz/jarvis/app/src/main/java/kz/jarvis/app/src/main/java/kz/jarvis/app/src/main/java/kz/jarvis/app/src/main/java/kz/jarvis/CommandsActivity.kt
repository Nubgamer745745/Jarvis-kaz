package kz.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class CommandsActivity : ComponentActivity() {

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val repo = CommandRepository(this)

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
                CommandsScreen(repo)
            }
        }
    }
}

@Composable
fun CommandsScreen(repo: CommandRepository) {
    val scope = rememberCoroutineScope()
    var list by remember { mutableStateOf<List<Command>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Command?>(null) }

    LaunchedEffect(Unit) {
        repo.getAll().collect { list = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "🤖 Командалар",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFCC)
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { editing = null; showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFCC),
                    contentColor = Color(0xFF0A0E27)
                )
            ) { Text("➕ Қосу") }
        }

        Spacer(Modifier.height(16.dp))

        if (list.isEmpty()) {
            Text(
                "Әзірге команда жоқ",
                color = Color.Gray,
                modifier = Modifier.padding(32.dp)
            )
        }

        LazyColumn {
            items(list) { cmd ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF111633)
                    )
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "🎤 \"${cmd.trigger}\"",
                                color = Color(0xFF00FFCC),
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "→ ${cmd.type}: ${cmd.action}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = {
                            editing = cmd
                            showDialog = true
                        }) {
                            Text("✏️", fontSize = 20.sp)
                        }
                        IconButton(onClick = {
                            scope.launch { repo.delete(cmd) }
                        }) {
                            Text("🗑️", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CommandDialog(
            initial = editing,
            onSave = { c ->
                scope.launch {
                    if (editing == null) repo.insert(c)
                    else repo.update(c.copy(id = editing!!.id))
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun CommandDialog(
    initial: Command?,
    onSave: (Command) -> Unit,
    onDismiss: () -> Unit
) {
    var trigger by remember { mutableStateOf(initial?.trigger ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: CommandTypes.APP) }
    var action by remember { mutableStateOf(initial?.action ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "Жаңа команда" else "Команданы өзгерту")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Дауыс командасы (мыс: ютуб аш)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Text("Түрі:", color = Color(0xFF00FFCC), fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))

                val types = listOf(
                    CommandTypes.APP to "Қолданба",
                    CommandTypes.URL to "Сайт",
                    CommandTypes.KEY to "Батырма",
                    CommandTypes.CALL to "Қоңырау",
                    CommandTypes.SMS to "SMS",
                    CommandTypes.SPEAK to "Сөйлеу",
                    CommandTypes.CUSTOM to "Өзге"
                )

                LazyColumn(Modifier.heightIn(max = 150.dp)) {
                    items(types) { (t, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = type == t,
                                onClick = { type = t }
                            )
                            Text(label, color = Color(0xFF00FFCC), fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Мәні (мыс: com.google.android.youtube)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (trigger.isNotBlank() && action.isNotBlank()) {
                    onSave(Command(
                        trigger = trigger.trim(),
                        type = type,
                        action = action.trim()
                    ))
                }
            }) { Text("💾 Сақтау") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Болдырмау") }
        }
    )
}
