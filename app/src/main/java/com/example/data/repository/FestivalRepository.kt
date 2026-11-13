package com.example.data.repository

import com.example.data.catalog.BundledCatalog
import com.example.data.local.DailyPanchangDao
import com.example.data.local.DailyPanchangEntity
import com.example.data.local.FestivalDao
import com.example.data.local.FestivalEntity
import com.example.data.local.MuhuratDao
import com.example.data.local.MuhuratEntity
import com.example.data.local.SamagriProgressDao
import com.example.data.local.SamagriProgressEntity
import com.example.domain.model.*
import com.example.panchang.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FestivalRepository(
    private val festivalDao: FestivalDao,
    private val samagriProgressDao: SamagriProgressDao,
    private val dailyPanchangDao: DailyPanchangDao? = null,
    private val muhuratDao: MuhuratDao? = null
) {
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    suspend fun ensureCatalogLoaded() {
        festivalDao.deleteSynthesizedCustomEntries()
        festivalDao.insertAll(BundledCatalog.FESTIVALS)
        festivalDao.insertAll(
            com.example.data.remote.OnlineFestivalDiscoveryService
                .getAllEncyclopedicFestivals()
                .map { it.entity }
        )
    }

    suspend fun addOrUpdateFestival(entity: FestivalEntity) {
        festivalDao.insertAll(listOf(entity))
    }

    fun getAllFestivals(): Flow<List<Festival>> {
        return festivalDao.getAllFestivals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getFestivalById(id: String): Flow<Festival?> {
        return festivalDao.getFestivalById(id).map { it?.toDomain() }
    }

    suspend fun getFestivalByIdDirect(id: String): Festival? {
        return festivalDao.getFestivalByIdDirect(id)?.toDomain()
    }

    suspend fun getAndCacheDayPanchang(date: LocalDate, city: City): PanchangDay {
        val panchang = PanchangEngine.computeDayPanchang(date, city)
        try {
            val isoDate = date.toString()
            val entity = DailyPanchangEntity(
                date = isoDate,
                cityId = city.id,
                dayOfWeek = date.dayOfWeek.name,
                tithiIndex = panchang.tithi.index,
                tithiName = panchang.tithi.name,
                paksha = panchang.tithi.paksha.name,
                tithiEndTime = panchang.tithi.endTime,
                nakshatraIndex = panchang.nakshatra.index,
                nakshatraName = panchang.nakshatra.name,
                yogaName = panchang.yoga.name,
                karanaName = panchang.karana.name,
                sunrise = panchang.formattedSunrise(),
                sunset = panchang.formattedSunset(),
                rahuKaalStart = panchang.rahuKaal.format().substringBefore(" - "),
                rahuKaalEnd = panchang.rahuKaal.format().substringAfter(" - "),
                abhijitStart = panchang.abhijitMuhurat?.format()?.substringBefore(" - "),
                abhijitEnd = panchang.abhijitMuhurat?.format()?.substringAfter(" - "),
                amantaMonth = panchang.amantaMonth,
                purnimantaMonth = panchang.purnimantaMonth,
                ayanamsaDegrees = panchang.ayanamsaDegrees
            )
            dailyPanchangDao?.insert(entity)

            val muhurats = mutableListOf<MuhuratEntity>()
            val brahmaFormatted = panchang.brahmaMuhurat.format()
            muhurats.add(
                MuhuratEntity(
                    festivalId = null,
                    date = isoDate,
                    cityId = city.id,
                    title = "Brahma Muhurat",
                    description = "Pre-dawn sacred window for meditation and Sankalpa",
                    muhuratType = "BRAHMA_MUHURAT",
                    startTime = brahmaFormatted.substringBefore(" - "),
                    endTime = brahmaFormatted.substringAfter(" - "),
                    isAuspicious = true
                )
            )
            panchang.abhijitMuhurat?.let { abhijit ->
                val formatted = abhijit.format()
                muhurats.add(
                    MuhuratEntity(
                        festivalId = null,
                        date = isoDate,
                        cityId = city.id,
                        title = "Abhijit Muhurat",
                        description = "Midday victorious solar window",
                        muhuratType = "ABHIJIT",
                        startTime = formatted.substringBefore(" - "),
                        endTime = formatted.substringAfter(" - "),
                        isAuspicious = true
                    )
                )
            }
            val rahuFormatted = panchang.rahuKaal.format()
            muhurats.add(
                MuhuratEntity(
                    festivalId = null,
                    date = isoDate,
                    cityId = city.id,
                    title = "Rahu Kaal",
                    description = "Avoid initiating new auspicious rituals",
                    muhuratType = "RAHU_KAAL",
                    startTime = rahuFormatted.substringBefore(" - "),
                    endTime = rahuFormatted.substringAfter(" - "),
                    isAuspicious = false
                )
            )
            val yamaFormatted = panchang.yamagandam.format()
            muhurats.add(
                MuhuratEntity(
                    festivalId = null,
                    date = isoDate,
                    cityId = city.id,
                    title = "Yamagandam",
                    description = "Inauspicious daytime segment for new beginnings",
                    muhuratType = "YAMAGANDAM",
                    startTime = yamaFormatted.substringBefore(" - "),
                    endTime = yamaFormatted.substringAfter(" - "),
                    isAuspicious = false
                )
            )
            val gulikaFormatted = panchang.gulikaKaal.format()
            muhurats.add(
                MuhuratEntity(
                    festivalId = null,
                    date = isoDate,
                    cityId = city.id,
                    title = "Gulika Kaal",
                    description = "Saturn-ruled daytime segment",
                    muhuratType = "GULIKA_KAAL",
                    startTime = gulikaFormatted.substringBefore(" - "),
                    endTime = gulikaFormatted.substringAfter(" - "),
                    isAuspicious = false
                )
            )
            muhuratDao?.deleteForDate(isoDate)
            muhuratDao?.insertAll(muhurats)
        } catch (_: Exception) {
            // Best-effort offline cache
        }
        return panchang
    }

    fun getSamagriProgress(festivalId: String, year: Int): Flow<SamagriProgress> {
        return samagriProgressDao.getProgress(festivalId, year).map { entity ->
            entity?.toDomain() ?: SamagriProgress(festivalId = festivalId, year = year, checkedItemIds = emptySet())
        }
    }

    suspend fun toggleSamagriItem(festivalId: String, year: Int, itemId: String) {
        val current = samagriProgressDao.getProgressDirect(festivalId, year)
        val currentChecked = current?.toDomain()?.checkedItemIds?.toMutableSet() ?: mutableSetOf()

        if (currentChecked.contains(itemId)) {
            currentChecked.remove(itemId)
        } else {
            currentChecked.add(itemId)
        }

        val updatedEntity = SamagriProgressEntity(
            festivalId = festivalId,
            year = year,
            checkedItemIdsJson = JSONArray(currentChecked).toString(),
            notes = current?.notes ?: "",
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        samagriProgressDao.upsert(updatedEntity)
    }

    suspend fun updateNotes(festivalId: String, year: Int, notes: String) {
        val current = samagriProgressDao.getProgressDirect(festivalId, year)
        val currentChecked = current?.checkedItemIdsJson ?: "[]"
        val updated = SamagriProgressEntity(
            festivalId = festivalId,
            year = year,
            checkedItemIdsJson = currentChecked,
            notes = notes,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        samagriProgressDao.upsert(updated)
    }

    suspend fun resetSamagriChecklist(festivalId: String, year: Int) {
        val current = samagriProgressDao.getProgressDirect(festivalId, year)
        val updated = SamagriProgressEntity(
            festivalId = festivalId,
            year = year,
            checkedItemIdsJson = "[]",
            notes = current?.notes ?: "",
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        samagriProgressDao.upsert(updated)
    }

    suspend fun clearAllLocalData() {
        samagriProgressDao.deleteAll()
        dailyPanchangDao?.deleteAll()
        muhuratDao?.deleteAll()
    }

    /**
     * Resolves festival occurrences for a specific city and date range.
     */
    fun computeOccurrencesForYear(
        festivals: List<Festival>,
        year: Int,
        city: City
    ): List<FestivalOccurrence> {
        return festivals.map { festival ->
            val resolvedDate = FestivalDateEvaluator.resolveFestivalDate(
                festivalId = festival.id,
                year = year,
                city = city,
                rule = festival.dateRule
            )
            val muhuratWindow = formatMuhuratForCity(festival, resolvedDate, city)
            val parsedRange = MuhuratValidationState.parseTimeRange(muhuratWindow)
            val muhuratState = if (parsedRange != null) {
                MuhuratValidationState.createVerified(
                    festivalDate = resolvedDate,
                    cityId = city.id,
                    startTime = parsedRange.startTime,
                    endTime = parsedRange.endTime,
                    verificationProvenance = "Drik Ganita / Lahiri Chitrapaksha Meeus Ephemeris",
                    formattedWindow = muhuratWindow
                )
            } else {
                MuhuratValidationState.resolve(muhuratWindow)
            }
            FestivalOccurrence(
                festival = festival,
                date = resolvedDate,
                muhuratWindowFormatted = muhuratWindow,
                muhuratState = muhuratState
            )
        }.sortedBy { it.date }
    }

    fun formatMuhuratForCity(festival: Festival, date: LocalDate, city: City): String {
        return PanchangEngine.computeFestivalMuhuratWindow(
            festivalId = festival.id,
            muhuratType = festival.muhuratType,
            dateRule = festival.dateRule,
            date = date,
            city = city
        ).format()
    }

    fun formatMuhuratBreakdownForCity(festival: Festival, date: LocalDate, city: City): String {
        return PanchangEngine.computeFestivalMuhuratBreakdown(
            festivalId = festival.id,
            muhuratType = festival.muhuratType,
            dateRule = festival.dateRule,
            date = date,
            city = city
        )
    }
}

fun FestivalEntity.toDomain(): Festival {
    val vidhiSteps = parseVidhiSteps(vidhiJson)
    val samagriItems = parseSamagriItems(samagriJson)
    val aliasList = aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val traditionList = traditions.split(",").mapNotNull { id ->
        RegionalTradition.values().firstOrNull { it.id.equals(id.trim(), ignoreCase = true) || it.name.equals(id.trim(), ignoreCase = true) }
    }
    val pakshaEnum = try {
        Paksha.valueOf(paksha.uppercase())
    } catch (e: Exception) {
        Paksha.SHUKLA
    }
    val obsTime = try {
        ObservanceTime.valueOf(observanceTime.uppercase())
    } catch (e: Exception) {
        ObservanceTime.SUNRISE
    }

    return Festival(
        id = id,
        version = version,
        nameEnglish = nameEnglish,
        nameNative = nameNative,
        nativeScriptLabel = nativeScriptLabel,
        aliases = aliasList,
        traditions = traditionList,
        dateRule = FestivalDateRule(
            lunarMonth = lunarMonth,
            paksha = pakshaEnum,
            tithiNumber = tithiNumber,
            observanceTime = obsTime,
            solarFixedMonth = solarFixedMonth,
            solarFixedDay = solarFixedDay
        ),
        muhuratTitle = muhuratTitle,
        muhuratDescription = muhuratDescription,
        muhuratType = muhuratType,
        vidhiSteps = vidhiSteps,
        samagriItems = samagriItems,
        significance = significance,
        sources = sources,
        reviewedBy = reviewedBy
    )
}

fun SamagriProgressEntity.toDomain(): SamagriProgress {
    val checkedSet = mutableSetOf<String>()
    try {
        val array = JSONArray(checkedItemIdsJson)
        for (i in 0 until array.length()) {
            checkedSet.add(array.getString(i))
        }
    } catch (e: Exception) {
        checkedItemIdsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            checkedSet.add(it)
        }
    }
    return SamagriProgress(
        festivalId = festivalId,
        year = year,
        checkedItemIds = checkedSet,
        notes = notes,
        updatedAt = updatedAt
    )
}

private fun parseVidhiSteps(json: String): List<VidhiStep> {
    val list = mutableListOf<VidhiStep>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                VidhiStep(
                    stepNumber = obj.optInt("stepNumber", i + 1),
                    title = obj.getString("title"),
                    body = obj.getString("body"),
                    durationMinutes = if (obj.has("durationMinutes")) obj.getInt("durationMinutes") else null
                )
            )
        }
    } catch (_: Exception) {
    }
    if (list.isEmpty()) {
        val objRegex = Regex("""\{[^{}]*\}""")
        val stepRegex = Regex(""""stepNumber"\s*:\s*(\d+)""")
        val titleRegex = Regex(""""title"\s*:\s*"([^"]*)"""")
        val bodyRegex = Regex(""""body"\s*:\s*"([^"]*)"""")
        val durationRegex = Regex(""""durationMinutes"\s*:\s*(\d+)""")
        objRegex.findAll(json).forEachIndexed { index, match ->
            val block = match.value
            val title = titleRegex.find(block)?.groupValues?.get(1)
            val body = bodyRegex.find(block)?.groupValues?.get(1)
            if (title != null && body != null) {
                list.add(
                    VidhiStep(
                        stepNumber = stepRegex.find(block)?.groupValues?.get(1)?.toIntOrNull() ?: (index + 1),
                        title = title,
                        body = body,
                        durationMinutes = durationRegex.find(block)?.groupValues?.get(1)?.toIntOrNull()
                    )
                )
            }
        }
    }
    if (list.isEmpty()) {
        list.add(VidhiStep(1, "Puja Vidhi", "Perform sacred puja with pure intention."))
    }
    return list
}

private fun parseSamagriItems(json: String): List<SamagriItem> {
    val list = mutableListOf<SamagriItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SamagriItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    quantity = obj.optString("quantity", null),
                    notes = obj.optString("notes", null)
                )
            )
        }
    } catch (_: Exception) {
    }
    if (list.isEmpty()) {
        val objRegex = Regex("""\{[^{}]*\}""")
        val idRegex = Regex(""""id"\s*:\s*"([^"]*)"""")
        val nameRegex = Regex(""""name"\s*:\s*"([^"]*)"""")
        val qtyRegex = Regex(""""quantity"\s*:\s*"([^"]*)"""")
        val notesRegex = Regex(""""notes"\s*:\s*"([^"]*)"""")
        objRegex.findAll(json).forEachIndexed { index, match ->
            val block = match.value
            val name = nameRegex.find(block)?.groupValues?.get(1)
            if (name != null) {
                list.add(
                    SamagriItem(
                        id = idRegex.find(block)?.groupValues?.get(1) ?: "item_$index",
                        name = name,
                        quantity = qtyRegex.find(block)?.groupValues?.get(1),
                        notes = notesRegex.find(block)?.groupValues?.get(1)
                    )
                )
            }
        }
    }
    if (list.isEmpty()) {
        list.add(SamagriItem("default", "Puja Thali & Diya"))
    }
    return list
}

