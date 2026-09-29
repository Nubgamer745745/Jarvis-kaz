package kz.jarvis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import kotlinx.coroutines.delay

class CommandExecutor(private val ctx: Context) {

    suspend fun execute(cmd: Command) {
        try {
            when (cmd.type) {
                CommandTypes.APP -> openApp(cmd.action)
                CommandTypes.URL -> openUrl(cmd.action)
                CommandTypes.CALL -> call(cmd.action)
                CommandTypes.SMS -> sms(cmd.action)
                CommandTypes.KEY -> key(cmd.action)
                CommandTypes.TEXT -> AccessibilityHelper.typeText(cmd.action)
                CommandTypes.SPEAK -> VoiceSpeaker(ctx).speak(cmd.action)
                CommandTypes.CUSTOM -> custom(cmd.action)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun executeAi(actions: List<AiClient.AiAction>) {
        for (a in actions) {
            try {
                when (a.type) {
                    "open_app" -> a.`package`?.let { openApp(it) }
                    "tap" -> AccessibilityHelper.tap(a.x ?: 0, a.y ?: 0)
                    "long_press" -> AccessibilityHelper.longPress(a.x ?: 0, a.y ?: 0)
                    "swipe" -> AccessibilityHelper.swipe(
                        a.x1 ?: 0, a.y1 ?: 0, a.x2 ?: 0, a.y2 ?: 0
                    )
                    "type" -> a.text?.let { AccessibilityHelper.typeText(it) }
                    "click_text" -> a.text?.let { AccessibilityHelper.clickByText(it) }
                    "key" -> a.key?.let { key(it) }
                    "wait" -> delay(((a.seconds ?: 1) * 1000).toLong())
                    "speak" -> a.text?.let { VoiceSpeaker(ctx).speak(it) }
                }
                delay(400)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openApp(pkg: String) {
        val i = ctx.packageManager.getLaunchIntentForPackage(pkg)
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        }
    }

    private fun openUrl(url: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun call(num: String) {
        try {
            val i = Intent(Intent.ACTION_CALL, Uri.parse("tel:$num"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun sms(data: String) {
        val p = data.split("|")
        if (p.size == 2) {
            try {
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${p[0]}"))
                i.putExtra("sms_body", p[1])
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(i)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun key(k: String) {
        when (k.lowercase()) {
            "home", "үй", "үйге" -> AccessibilityHelper.home()
            "back", "артқа" -> AccessibilityHelper.back()
            "recents", "соңғы" -> AccessibilityHelper.recents()
            "notifications", "хабарлама" -> AccessibilityHelper.notifications()
        }
    }

    private fun custom(cmd: String) {
        when (cmd.lowercase()) {
            "wifi_on", "вайфай_қос" -> setWifi(true)
            "wifi_off", "вайфай_өшір" -> setWifi(false)
            "home" -> AccessibilityHelper.home()
            "back" -> AccessibilityHelper.back()
        }
    }

    @Suppress("DEPRECATION")
    private fun setWifi(on: Boolean) {
        try {
            val wm = ctx.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            wm.isWifiEnabled = on
        } catch (e: Exception) { e.printStackTrace() }
    }
}
