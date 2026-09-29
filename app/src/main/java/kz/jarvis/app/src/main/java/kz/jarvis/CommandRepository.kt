package kz.jarvis

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM commands ORDER BY id DESC")
    fun getAll(): Flow<List<Command>>

    @Query("SELECT * FROM commands WHERE enabled = 1")
    suspend fun findAll(): List<Command>

    @Insert
    suspend fun insert(c: Command): Long

    @Update
    suspend fun update(c: Command)

    @Delete
    suspend fun delete(c: Command)
}

@Database(entities = [Command::class], version = 1)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    companion object {
        @Volatile private var I: JarvisDatabase? = null
        fun get(ctx: Context): JarvisDatabase = I ?: synchronized(this) {
            I ?: Room.databaseBuilder(ctx.applicationContext,
                JarvisDatabase::class.java, "jarvis_db").build().also { I = it }
        }
    }
}

class CommandRepository(ctx: Context) {
    private val dao = JarvisDatabase.get(ctx).commandDao()

    fun getAll() = dao.getAll()
    suspend fun findAll() = dao.findAll()
    suspend fun insert(c: Command) = dao.insert(c)
    suspend fun update(c: Command) = dao.update(c)
    suspend fun delete(c: Command) = dao.delete(c)

    suspend fun findMatching(text: String): Command? {
        val lower = text.lowercase()
        return dao.findAll().firstOrNull {
            lower.contains(it.trigger.lowercase())
        }
    }
}
