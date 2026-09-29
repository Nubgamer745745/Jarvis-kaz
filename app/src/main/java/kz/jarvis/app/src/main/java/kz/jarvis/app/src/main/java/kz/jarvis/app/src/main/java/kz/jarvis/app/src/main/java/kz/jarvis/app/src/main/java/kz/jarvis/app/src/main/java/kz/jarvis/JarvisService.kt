package kz.jarvis

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class JarvisService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repo: CommandRepository
    private lateinit var recognizer: VoiceRecognizer
    private lateinit var speaker: VoiceSpeaker
    private lateinit var executor: CommandExecutor
    private lateinit var ai: AiClient

    private val wakeWords = listOf("jarvis", "джарвис", "жарвис", "жарвіс")
    private var running = false

    override fun onCreate() {
        super.onCreate()
        repo = CommandRepository(this)
        recognizer = VoiceRecognizer(this)
        speaker = VoiceSpeaker(this)
        executor = CommandExecutor(this)
        val key = getSharedPreferences("jarvis", MODE_PRIVATE)
            .getString("api_key", "") ?: ""
        ai = AiClient(key)
        startForegroundNotification()
    }

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        if (!running) {
            running = true
            loop()
        }
        return START_STICKY
    }

    private fun loop() = scope.launch {
        while (running) {
            try {
                val heard = recognizer.listen()

                if (heard.isBlank()) {
                    delay(300)
                    continue
                }

                if (wakeWords.any { heard.lowercase().contains(it) }) {
                    speaker.speak("Иә, мырзам?")
                    delay(700)

                    val cmd = recognizer.listen()
                    if (cmd.isBlank()) continue

                    handle(cmd)
                }
            } catch (e: Exception) {
                delay(800)
            }
        }
    }

    private suspend fun handle(text: String) {
        // Алдымен сақталған командалардан іздейміз
        val saved = repo.findMatching(text)
        if (saved != null) {
            executor.execute(saved)
            speaker.speak("Орындалды, мырзам")
            return
        }

        // Жоқ болса — ИИ-ге жібереміз
        val resp = ai.ask(text)
        executor.executeAi(resp.actions)
        resp.speech?.let { speaker.speak(it) }
    }

    private fun startForegroundNotification() {
        val ch = "jarvis_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(
                NotificationChannel(
                    ch,
                    "Jarvis",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        val tapIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val n = NotificationCompat.Builder(this, ch)
            .setContentTitle("Jarvis жұмыс істеп тұр")
            .setContentText("Дауыс командасын күтіп тұр")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()

        startForeground(1, n)
    }

    override fun onDestroy() {
        running = false
        scope.cancel()
        speaker.shutdown()
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null
}
