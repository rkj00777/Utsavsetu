package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "festivals")
data class FestivalEntity(
    @PrimaryKey val id: String,
    val version: Int,
    val nameEnglish: String,
    val nameNative: String,
    val nativeScriptLabel: String,
    val aliases: String,               // comma-separated list of search keywords
    val traditions: String,           // comma-separated RegionalTradition IDs
    val lunarMonth: String,
    val paksha: String,
    val tithiNumber: Int,
    val observanceTime: String,
    val solarFixedMonth: Int? = null,
    val solarFixedDay: Int? = null,
    val muhuratTitle: String,
    val muhuratDescription: String,
    val muhuratType: String,          // "MIDDAY", "PRADOSH", "NISHITA", "SUNRISE", "ALL_DAY"
    val vidhiJson: String,            // Serialized List<VidhiStep>
    val samagriJson: String,          // Serialized List<SamagriItem>
    val significance: String,
    val sources: String,
    val reviewedBy: String
)

@Entity(tableName = "samagri_progress", primaryKeys = ["festivalId", "year"])
data class SamagriProgressEntity(
    val festivalId: String,
    val year: Int,
    val checkedItemIdsJson: String,   // comma-separated or JSON list of checked item IDs
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
