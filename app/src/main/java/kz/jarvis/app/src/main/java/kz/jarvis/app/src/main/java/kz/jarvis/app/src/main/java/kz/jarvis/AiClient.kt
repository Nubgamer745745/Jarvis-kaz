package kz.jarvis

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiClient(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val sysPrompt = """
        Сен Jarvis — дауыспен басқарылатын қазақ тілді ИИ көмекші.
        Сен ТЕК ҚАЗАҚ ТІЛІНДЕ жауап бересің. Басқа тілде сөйлемейсің.

        Пайдаланушының телефонын басқарасың.

        Тек JSON форматында жауап бер:
        {
          "actions": [
            {"type": "open_app", "package": "com.google.android.youtube"},
            {"type": "tap", "x": 500, "y": 1200},
            {"type": "type", "text": "сәлем"},
            {"type": "swipe", "x1": 500, "y1": 1500, "x2": 500, "y2": 500},
            {"type": "key", "key": "home"},
            {"type": "wait", "seconds": 1},
            {"type": "speak", "text": "Дайын, мырзам"}
          ],
          "speech": "не айту керек (ТЕК ҚАЗАҚША)"
        }

        Танымал қолданбалар:
        - YouTube: com.google.android.youtube
        - WhatsApp: com.whatsapp
        - Telegram: org.telegram.messenger
        - Chrome: com.android.chrome
        - Камера: com.android.camera
        - Параметрлер: com.android.settings
        - Телефон: com.android.dialer
        - SMS: com.android.mms
        - Instagram: com.instagram.android
        - VK: com.vkontakte.android
        - TikTok: com.zhiliaoapp.musically
        - Spotify: com.spotify.music
    """.trimIndent()

    data class AiAction(
        val type: String,
        val `package`: String? = null,
        val x: Int? = null, val y: Int? = null,
        val x1: Int? = null, val y1: Int? = null,
        val x2: Int? = null, val y2: Int? = null,
        val text: String? = null,
        val key: String? = null,
        val seconds: Int? = null
    )

    data class AiResponse(
        val actions: List<AiAction> = emptyList(),
        val speech: String? = null
    )

    suspend fun ask(userText: String, screenBase64: String? = null): AiResponse =
        withContext(Dispatchers.IO) {
            try {
                val messages = mutableListOf<Map<String, Any>>()

                messages.add(mapOf("role" to "system", "content" to sysPrompt))

                if (screenBase64 != null) {
                    messages.add(mapOf(
                        "role" to "user",
                        "content" to listOf(
                            mapOf("type" to "text", "text" to userText),
                            mapOf("type" to "image_url", "image_url" to
                                mapOf("url" to "data:image/jpeg;base64,$screenBase64"))
                        )
                    ))
                } else {
                    messages.add(mapOf("role" to "user", "content" to userText))
                }

                val body = mapOf(
                    "model" to "gpt-4o",
                    "messages" to messages,
                    "response_format" to mapOf("type" to "json_object"),
                    "max_tokens" to 500,
                    "temperature" to 0.7
                )

                val req = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(gson.toJson(body).toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(req).execute().use { resp ->
                    val json = resp.body?.string() ?: "{}"
                    val obj = gson.fromJson(json, JsonObject::class.java)
                    val content = obj["choices"]?.asJsonArray?.get(0)
                        ?.asJsonObject?.get("message")?.asJsonObject
                        ?.get("content")?.asString ?: "{}"
                    gson.fromJson(content, AiResponse::class.java)
                }
            } catch (e: Exception) {
                AiResponse(speech = "Кешіріңіз, қате шықты")
            }
        }
}
