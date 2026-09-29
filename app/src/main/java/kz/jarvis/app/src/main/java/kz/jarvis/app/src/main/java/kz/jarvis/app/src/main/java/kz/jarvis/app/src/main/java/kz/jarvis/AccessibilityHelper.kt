package kz.jarvis

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityHelper : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    companion object {
        private var instance: AccessibilityHelper? = null

        fun isReady(): Boolean = instance != null

        fun globalAction(action: Int): Boolean =
            instance?.performGlobalAction(action) ?: false

        fun tap(x: Int, y: Int) {
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val g = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                .build()
            instance?.dispatchGesture(g, null, null)
        }

        fun longPress(x: Int, y: Int) {
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val g = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
                .build()
            instance?.dispatchGesture(g, null, null)
        }

        fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) {
            val path = Path().apply {
                moveTo(x1.toFloat(), y1.toFloat())
                lineTo(x2.toFloat(), y2.toFloat())
            }
            val g = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
                .build()
            instance?.dispatchGesture(g, null, null)
        }

        fun typeText(text: String) {
            val root = instance?.rootInActiveWindow ?: return
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: root
            val args = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
            }
            focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        fun clickByText(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val nodes = root.findAccessibilityNodeInfosByText(text)
            if (nodes.isNullOrEmpty()) return false
            val node = nodes.firstOrNull { it.isClickable }
                ?: nodes.firstOrNull()
                ?: return false
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        fun back(): Boolean = globalAction(GLOBAL_ACTION_BACK)
        fun home(): Boolean = globalAction(GLOBAL_ACTION_HOME)
        fun recents(): Boolean = globalAction(GLOBAL_ACTION_RECENTS)
        fun notifications(): Boolean = globalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }
}
