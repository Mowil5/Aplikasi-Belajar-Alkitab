package com.example

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Entity(tableName = "scripture_cache")
data class ScriptureCache(
    @PrimaryKey val queryPassage: String, // Normalized lowercase query, e.g. "yohanes3:16", "matius28:19"
    val reference: String,               // Clean official name, e.g. "Yohanes 3:16"
    val textTb: String,                  // Indonesian TB text
    val textKjv: String,                  // English KJV/NKJV text
    val analysisJson: String?,            // Cached theological analysis result (from Gemini) - nullable
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ScriptureCacheDao {
    @Query("SELECT * FROM scripture_cache WHERE queryPassage = :query LIMIT 1")
    suspend fun getCacheByQuery(query: String): ScriptureCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: ScriptureCache)

    @Query("SELECT * FROM scripture_cache ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScriptureCache>>

    @Query("DELETE FROM scripture_cache")
    suspend fun clearAll()
}

@Database(entities = [ScriptureCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptureCacheDao(): ScriptureCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offline_bible_cache_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class OfflineBibleRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.scriptureCacheDao()

    fun getAllHistory(): Flow<List<ScriptureCache>> = dao.getAllHistory()

    suspend fun getCache(passageQuery: String): ScriptureCache? {
        val normalized = normalizeQuery(passageQuery)
        val local = dao.getCacheByQuery(normalized)
        if (local != null) return local

        // If newly installed and empty, pre-populate standard verses so common searches are instant!
        checkAndPrepopulate()
        return dao.getCacheByQuery(normalized)
    }

    suspend fun saveCache(queryPassage: String, reference: String, textTb: String, textKjv: String, analysisJson: String?) {
        val normalized = normalizeQuery(queryPassage)
        val entry = ScriptureCache(
            queryPassage = normalized,
            reference = reference,
            textTb = textTb,
            textKjv = textKjv,
            analysisJson = analysisJson
        )
        withContext(Dispatchers.IO) {
            dao.insertCache(entry)
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            dao.clearAll()
        }
    }

    fun normalizeQuery(query: String): String {
        return query.lowercase()
            .replace(" ", "")
            .replace("-", "")
            .replace(":", "")
            .replace(":", "")
            .trim()
    }

    private suspend fun checkAndPrepopulate() {
        // Just checking if any data exists, if not, insert initial seeds.
        val testSeed = dao.getCacheByQuery("yohanes316")
        if (testSeed == null) {
            val seeds = listOf(
                ScriptureCache(
                    queryPassage = "yohanes316",
                    reference = "Yohanes 3:16",
                    textTb = "Karena begitu besar kasih Allah akan dunia ini, sehingga Ia telah mengaruniakan Anak-Nya yang tunggal, supaya setiap orang yang percaya kepada-Nya tidak binasa, melainkan beroleh hidup yang kekal.",
                    textKjv = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "kejadian11",
                    reference = "Kejadian 1:1",
                    textTb = "Pada mulanya Allah menciptakan langit dan bumi.",
                    textKjv = "In the beginning God created the heaven and the earth.",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "mazmur231",
                    reference = "Mazmur 23:1",
                    textTb = "TUHAN adalah gembalaku, takkan kekurangan aku.",
                    textKjv = "The LORD is my shepherd; I shall not want.",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "roma323",
                    reference = "Roma 3:23",
                    textTb = "Karena semua orang telah berbuat dosa dan telah kehilangan kemuliaan Allah,",
                    textKjv = "For all have sinned, and come short of the glory of God;",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "roma623",
                    reference = "Roma 6:23",
                    textTb = "Sebab upah dosa ialah maut; tetapi karunia Allah ialah hidup yang kekal dalam Kristus Yesus, Tuhan kita.",
                    textKjv = "For the wages of sin is death; but the gift of God is eternal life through Jesus Christ our Lord.",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "efesus28",
                    reference = "Efesus 2:8",
                    textTb = "Sebab karena kasih karunia kamu diselamatkan oleh iman; itu bukan hasil usahamu, tetapi pemberian Allah,",
                    textKjv = "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God:",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "efesus29",
                    reference = "Efesus 2:9",
                    textTb = "itu bukan hasil pekerjaanmu: jangan ada orang yang memegahkan diri.",
                    textKjv = "Not of works, lest any man should boast.",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "yohanes146",
                    reference = "Yohanes 14:6",
                    textTb = "Kata Yesus kepadanya: \"Akulah jalan dan kebenaran dan hidup. Tidak ada seorangpun yang datang kepada Bapa, kalau tidak melalui Aku.\"",
                    textKjv = "Jesus saith unto him, I am the way, the truth, and the life: no man cometh unto the Father, but by me.",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "matius2819",
                    reference = "Matius 28:19",
                    textTb = "Karena itu pergilah, jadikanlah semua bangsa murid-Ku dan baptislah mereka dalam nama Bapa dan Anak dan Roh Kudus,",
                    textKjv = "Go ye therefore, and teach all nations, baptizing them in the name of the Father, and of the Son, and of the Holy Ghost:",
                    analysisJson = null
                ),
                ScriptureCache(
                    queryPassage = "filipi413",
                    reference = "Filipi 4:13",
                    textTb = "Segala perkara dapat kutanggung di dalam Dia yang memberi kekuatan kepadaku.",
                    textKjv = "I can do all things through Christ which strengtheneth me.",
                    analysisJson = null
                )
            )

            withContext(Dispatchers.IO) {
                for (seed in seeds) {
                    dao.insertCache(seed)
                }
            }
        }
    }
}
