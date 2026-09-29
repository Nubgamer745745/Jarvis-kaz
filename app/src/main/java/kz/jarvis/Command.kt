package kz.jarvis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands")
data class Command(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trigger: String,
    val type: String,
    val action: String,
    val category: String = "Жалпы",
    val enabled: Boolean = true
)

object CommandTypes {
    const val APP = "app"
    const val URL = "url"
    const val KEY = "key"
    const val TEXT = "text"
    const val SPEAK = "speak"
    const val CUSTOM = "custom"
    const val CALL = "call"
    const val SMS = "sms"
}
