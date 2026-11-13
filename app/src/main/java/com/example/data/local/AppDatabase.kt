package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.catalog.BundledCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FestivalEntity::class,
        SamagriProgressEntity::class,
        DailyPanchangEntity::class,
        MuhuratEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun festivalDao(): FestivalDao
    abstract fun samagriProgressDao(): SamagriProgressDao
    abstract fun dailyPanchangDao(): DailyPanchangDao
    abstract fun muhuratDao(): MuhuratDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "utsavsetu.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                val dao = getInstance(context, scope).festivalDao()
                                dao.insertAll(BundledCatalog.FESTIVALS)
                                dao.insertAll(
                                    com.example.data.remote.OnlineFestivalDiscoveryService
                                        .getAllEncyclopedicFestivals()
                                        .map { it.entity }
                                )
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            scope.launch(Dispatchers.IO) {
                                val dao = getInstance(context, scope).festivalDao()
                                dao.insertAll(BundledCatalog.FESTIVALS)
                                dao.insertAll(
                                    com.example.data.remote.OnlineFestivalDiscoveryService
                                        .getAllEncyclopedicFestivals()
                                        .map { it.entity }
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
