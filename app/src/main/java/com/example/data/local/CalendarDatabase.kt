package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Entity representing sacred festivals in the calendar.
 */
@Entity(tableName = "calendar_festivals")
data class CalendarFestivalEntity(
    @PrimaryKey val id: String,
    val version: Int = 1,
    val nameEnglish: String,
    val nameNative: String,
    val nativeScriptLabel: String,
    val aliases: String,            // comma-separated search aliases (Hinglish/Tanglish)
    val traditions: String,        // comma-separated RegionalTradition IDs
    val lunarMonth: String,
    val paksha: String,
    val tithiNumber: Int,
    val observanceTime: String,
    val solarFixedMonth: Int? = null,
    val solarFixedDay: Int? = null,
    val significance: String,
    val vidhiJson: String = "[]",
    val samagriJson: String = "[]",
    val sources: String = "",
    val reviewedBy: String = ""
)

/**
 * Entity representing auspicious windows (Muhurats) for festivals and rituals.
 */
@Entity(
    tableName = "muhurats",
    indices = [
        Index(value = ["date", "cityId"]),
        Index(value = ["festivalId"])
    ]
)
data class MuhuratEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val festivalId: String? = null,
    val date: String,             // ISO date: "YYYY-MM-DD"
    val cityId: String,           // e.g., "bengaluru", "mumbai", "delhi"
    val title: String,            // e.g., "Pradosh Kaal Lakshmi Puja Window"
    val description: String,      // e.g., "Sthir Lagna auspicious period for puja"
    val muhuratType: String,      // "MADHYAHNA", "PRADOSH", "NISHITA", "ABHIJIT", "BRAHMA_MUHURAT", "ALL_DAY"
    val startTime: String,        // "HH:mm" formatted
    val endTime: String,          // "HH:mm" formatted
    val isAuspicious: Boolean = true
)

/**
 * Entity representing daily Panchang parameters (Tithi, Nakshatra, Yoga, Karana, Solar timings).
 */
@Entity(
    tableName = "daily_panchang",
    primaryKeys = ["date", "cityId"],
    indices = [
        Index(value = ["cityId", "date"])
    ]
)
data class DailyPanchangEntity(
    val date: String,             // ISO date: "YYYY-MM-DD"
    val cityId: String,           // e.g., "bengaluru", "delhi", "mumbai"
    val dayOfWeek: String,        // "Monday", "Tuesday", etc.
    // Tithi information
    val tithiIndex: Int,          // 1..30
    val tithiName: String,        // e.g. "Chaturthi", "Purnima", "Amavasya"
    val paksha: String,           // "SHUKLA" or "KRISHNA"
    val tithiEndTime: String? = null,
    // Nakshatra information
    val nakshatraIndex: Int,      // 1..27
    val nakshatraName: String,    // e.g. "Rohini", "Pushya"
    // Yoga & Karana
    val yogaName: String,         // e.g. "Siddhi", "Shubha"
    val karanaName: String,       // e.g. "Bava", "Balava"
    // Solar & Planetary timings
    val sunrise: String,          // "06:15 AM"
    val sunset: String,           // "06:42 PM"
    val rahuKaalStart: String,    // "07:30 AM"
    val rahuKaalEnd: String,      // "09:00 AM"
    val abhijitStart: String? = null,
    val abhijitEnd: String? = null,
    // Lunar Month conventions
    val amantaMonth: String,      // e.g. "Bhadrapada" (South/West India)
    val purnimantaMonth: String,  // e.g. "Ashwina" (North India)
    val ayanamsaDegrees: Double = 24.1,
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// DAOs
// ==========================================

@Dao
interface CalendarFestivalDao {
    @Query("SELECT * FROM calendar_festivals ORDER BY nameEnglish ASC")
    fun getAllFestivals(): Flow<List<CalendarFestivalEntity>>

    @Query("SELECT * FROM calendar_festivals WHERE id = :id LIMIT 1")
    fun getFestivalById(id: String): Flow<CalendarFestivalEntity?>

    @Query("SELECT * FROM calendar_festivals WHERE id = :id LIMIT 1")
    suspend fun getFestivalByIdDirect(id: String): CalendarFestivalEntity?

    @Query("SELECT * FROM calendar_festivals WHERE traditions LIKE '%' || :traditionId || '%'")
    fun getFestivalsForTradition(traditionId: String): Flow<List<CalendarFestivalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(festivals: List<CalendarFestivalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(festival: CalendarFestivalEntity)

    @Delete
    suspend fun delete(festival: CalendarFestivalEntity)

    @Query("DELETE FROM calendar_festivals")
    suspend fun deleteAll()
}

@Dao
interface MuhuratDao {
    @Query("SELECT * FROM muhurats WHERE date = :date AND cityId = :cityId ORDER BY startTime ASC")
    fun getMuhuratsForDateAndCity(date: String, cityId: String): Flow<List<MuhuratEntity>>

    @Query("SELECT * FROM muhurats WHERE festivalId = :festivalId AND cityId = :cityId LIMIT 1")
    fun getMuhuratForFestival(festivalId: String, cityId: String): Flow<MuhuratEntity?>

    @Query("SELECT * FROM muhurats WHERE date = :date AND cityId = :cityId AND isAuspicious = 1 ORDER BY startTime ASC")
    fun getAuspiciousMuhurats(date: String, cityId: String): Flow<List<MuhuratEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(muhurats: List<MuhuratEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(muhurat: MuhuratEntity)

    @Query("DELETE FROM muhurats WHERE date = :date")
    suspend fun deleteForDate(date: String)

    @Query("DELETE FROM muhurats")
    suspend fun deleteAll()
}

@Dao
interface DailyPanchangDao {
    @Query("SELECT * FROM daily_panchang WHERE date = :date AND cityId = :cityId LIMIT 1")
    fun getPanchang(date: String, cityId: String): Flow<DailyPanchangEntity?>

    @Query("SELECT * FROM daily_panchang WHERE date = :date AND cityId = :cityId LIMIT 1")
    suspend fun getPanchangDirect(date: String, cityId: String): DailyPanchangEntity?

    @Query("SELECT * FROM daily_panchang WHERE cityId = :cityId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getPanchangRange(cityId: String, startDate: String, endDate: String): Flow<List<DailyPanchangEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(panchang: DailyPanchangEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(panchangList: List<DailyPanchangEntity>)

    @Query("DELETE FROM daily_panchang WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)

    @Query("DELETE FROM daily_panchang")
    suspend fun deleteAll()
}

// ==========================================
// ROOM DATABASE
// ==========================================

@Database(
    entities = [
        CalendarFestivalEntity::class,
        MuhuratEntity::class,
        DailyPanchangEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CalendarDatabase : RoomDatabase() {

    abstract fun calendarFestivalDao(): CalendarFestivalDao
    abstract fun muhuratDao(): MuhuratDao
    abstract fun dailyPanchangDao(): DailyPanchangDao

    companion object {
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        fun getInstance(context: Context): CalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "utsavsetu_calendar.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
