package com.example.reminder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.BundledCatalog
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.data.repository.toDomain
import com.example.domain.model.FestivalOccurrence
import com.example.domain.model.MuhuratValidationState
import com.example.platform.notifications.ReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReminderSchedulerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        ReminderScheduler.clearAllActiveAlarmsForTesting(context)
    }

    @Test
    fun testScenario1_syntacticallyValidRangeDoesNotBecomeVerifiedWithoutProvenance() {
        // Syntactically valid range "10:00 AM - 12:00 PM" must produce CalculatedEstimate, NOT Verified
        val rawWindow = "10:00 AM - 12:00 PM"
        val state = MuhuratValidationState.resolve(rawWindow)

        assertTrue("Syntactically valid window without provenance must be CalculatedEstimate", state is MuhuratValidationState.CalculatedEstimate)
        assertFalse("Unverified window must NOT be schedulable", state.isSchedulable)
        assertFalse("Unverified window must NOT be Verified", state is MuhuratValidationState.Verified)
    }

    @Test
    fun testScenario2_verifiedMuhuratWindowWithProvenanceIsSchedulable() {
        val festivalDate = LocalDate.now().plusDays(10)
        val verifiedState = MuhuratValidationState.createVerified(
            festivalDate = festivalDate,
            cityId = "delhi",
            startTime = LocalTime.of(11, 5),
            endTime = LocalTime.of(13, 35),
            zoneId = ZoneId.of("Asia/Kolkata"),
            calculationConvention = "Drik Ganita / Chitrapaksha Lahiri",
            verificationProvenance = "Rashtriya Panchang (IMD PAC) 1948 Saka",
            formattedWindow = "11:05 AM - 01:35 PM"
        )

        assertTrue("State with provenance must be Verified", verifiedState is MuhuratValidationState.Verified)
        val verified = verifiedState as MuhuratValidationState.Verified
        assertTrue("Verified state must be valid", verified.isValid)
        assertTrue("Verified state must be schedulable", verifiedState.isSchedulable)
        assertFalse("Daytime window should not cross midnight", verified.crossesMidnight)
        assertEquals(festivalDate, verified.startInstant.toLocalDate())
        assertEquals(festivalDate, verified.endInstant.toLocalDate())
    }

    @Test
    fun testScenario3_midnightCrossingWindowMaintainsObservanceDate() {
        // Nishita Kaal (11:51 PM - 12:42 AM) on Maha Shivaratri
        val festivalDate = LocalDate.now().plusDays(15)
        val nishitaState = MuhuratValidationState.createVerified(
            festivalDate = festivalDate,
            cityId = "varanasi",
            startTime = LocalTime.of(23, 51),
            endTime = LocalTime.of(0, 42),
            zoneId = ZoneId.of("Asia/Kolkata"),
            calculationConvention = "Drik Ganita Nishita Kaal",
            verificationProvenance = "Kashi Vishwanath Mandir Panchanga",
            formattedWindow = "11:51 PM - 12:42 AM"
        )

        assertTrue("Midnight-crossing window with provenance must be Verified", nishitaState is MuhuratValidationState.Verified)
        val verified = nishitaState as MuhuratValidationState.Verified
        assertTrue("Chronological order must be valid across midnight", verified.isValid)
        assertTrue("Must be schedulable", nishitaState.isSchedulable)
        assertTrue("Must identify that it crosses midnight", verified.crossesMidnight)

        // Crucial: Festival observance date is preserved
        assertEquals("Festival date must remain the primary observance date", festivalDate, verified.festivalDate)
        assertEquals("Start instant date must match festival date", festivalDate, verified.startInstant.toLocalDate())
        assertEquals("End instant date must be following morning", festivalDate.plusDays(1), verified.endInstant.toLocalDate())
        assertTrue("End instant must be chronologically after start instant", verified.endInstant.isAfter(verified.startInstant))

        // Trigger time is 30 mins before 23:51, which is 23:21 on festivalDate
        val triggerTime = verified.startInstant.minusMinutes(30).toLocalDateTime()
        assertEquals(festivalDate, triggerTime.toLocalDate())
        assertEquals(23, triggerTime.hour)
        assertEquals(21, triggerTime.minute)
    }

    @Test
    fun testScenario4_invalidOrReversedTimingRangeCannotBeScheduled() {
        val festivalDate = LocalDate.now().plusDays(5)
        // Missing provenance
        val missingProvenanceState = MuhuratValidationState.createVerified(
            festivalDate = festivalDate,
            cityId = "delhi",
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            verificationProvenance = ""
        )
        assertFalse("Missing provenance must not be schedulable", missingProvenanceState.isSchedulable)

        // Malformed timing string
        val malformedState = MuhuratValidationState.resolve("Not a time window")
        assertTrue("Malformed string must be Unavailable", malformedState is MuhuratValidationState.Unavailable)
        assertFalse(malformedState.isSchedulable)

        // Blank and N/A
        assertTrue(MuhuratValidationState.resolve(null) is MuhuratValidationState.Unavailable)
        assertTrue(MuhuratValidationState.resolve("N/A") is MuhuratValidationState.Unavailable)
        assertTrue(MuhuratValidationState.resolve("Timing Unavailable") is MuhuratValidationState.Unavailable)
    }

    @Test
    fun testScenario5_missingOccurrenceDateCancelsStaleAlarmAndOmitsScheduling() {
        val entry = CustomReminderEntry(
            festivalId = "diwali_lakshmi_puja",
            festivalName = "Diwali",
            dateFormatted = "Nov 8, 2026",
            triggerType = "ON_DAY",
            timeLabel = "07:00 AM"
        )

        // Attempting to schedule with null festival date
        val scheduled = ReminderScheduler.scheduleCustomReminder(
            context = context,
            entry = entry,
            festivalDate = null,
            muhuratWindowFormatted = "05:32 PM - 07:28 PM"
        )

        assertFalse("Scheduling must return false when festival date is missing", scheduled)
        val code = ReminderScheduler.customReminderRequestCode(entry.festivalId, entry.triggerType)
        assertFalse("Alarm code must NOT be active", ReminderScheduler.isAlarmScheduled(context, code))
    }

    @Test
    fun testScenario6_unverifiedMuhuratSuppressesAlarmAndCancelsStaleAlarm() {
        val entry = CustomReminderEntry(
            festivalId = "diwali_lakshmi_puja",
            festivalName = "Diwali",
            dateFormatted = "Nov 8, 2026",
            triggerType = "MUHURAT_START",
            timeLabel = "At Muhurat"
        )

        // Unverified timing window "05:32 PM - 07:28 PM" passed without verified provenance
        val scheduled = ReminderScheduler.scheduleCustomReminder(
            context = context,
            entry = entry,
            festivalDate = LocalDate.now().plusDays(10),
            muhuratState = null,
            muhuratWindowFormatted = "05:32 PM - 07:28 PM"
        )

        assertFalse("Unverified muhurat must NOT be scheduled", scheduled)
        val code = ReminderScheduler.customReminderRequestCode(entry.festivalId, entry.triggerType)
        assertFalse("Alarm must NOT be registered", ReminderScheduler.isAlarmScheduled(context, code))
    }

    @Test
    fun testScenario7_alarmReconciliationCancelsDisappearedOrObsoleteAlarms() {
        val fest1 = BundledCatalog.FESTIVALS[0].toDomain()
        val fest2 = BundledCatalog.FESTIVALS[1].toDomain()

        val date1 = LocalDate.now().plusDays(5)
        val date2 = LocalDate.now().plusDays(10)

        val occ1 = FestivalOccurrence(fest1, date1, "11:00 AM - 01:00 PM")
        val occ2 = FestivalOccurrence(fest2, date2, "06:00 PM - 08:00 PM")

        // Step 1: Schedule rolling window with both festivals (day before and morning of enabled)
        ReminderScheduler.scheduleRollingWindow(
            context = context,
            occurrences = listOf(occ1, occ2),
            dayBeforeEnabled = true,
            morningOfEnabled = true,
            muhuratStartEnabled = false
        )

        val code1DayBefore = ReminderScheduler.rollingDayBeforeCode(fest1.id)
        val code1Morning = ReminderScheduler.rollingMorningCode(fest1.id)
        val code2DayBefore = ReminderScheduler.rollingDayBeforeCode(fest2.id)
        val code2Morning = ReminderScheduler.rollingMorningCode(fest2.id)

        val activeCodesStep1 = ReminderScheduler.getActiveRollingAlarmCodes(context)
        assertTrue("Fest1 day-before must be scheduled", activeCodesStep1.contains(code1DayBefore))
        assertTrue("Fest1 morning must be scheduled", activeCodesStep1.contains(code1Morning))
        assertTrue("Fest2 day-before must be scheduled", activeCodesStep1.contains(code2DayBefore))
        assertTrue("Fest2 morning must be scheduled", activeCodesStep1.contains(code2Morning))

        // Step 2: Fest2 is removed/disappears from occurrences list (e.g. filtered or outside window)
        ReminderScheduler.scheduleRollingWindow(
            context = context,
            occurrences = listOf(occ1), // occ2 omitted
            dayBeforeEnabled = true,
            morningOfEnabled = true,
            muhuratStartEnabled = false
        )

        val activeCodesStep2 = ReminderScheduler.getActiveRollingAlarmCodes(context)
        assertTrue("Fest1 day-before must remain scheduled", activeCodesStep2.contains(code1DayBefore))
        assertTrue("Fest1 morning must remain scheduled", activeCodesStep2.contains(code1Morning))
        assertFalse("Fest2 day-before must be cancelled and absent from active codes", activeCodesStep2.contains(code2DayBefore))
        assertFalse("Fest2 morning must be cancelled and absent from active codes", activeCodesStep2.contains(code2Morning))
    }

    @Test
    fun testScenario8_independentReminderTogglesReconcileProperly() {
        val fest = BundledCatalog.FESTIVALS[0].toDomain()
        val date = LocalDate.now().plusDays(7)
        val occ = FestivalOccurrence(fest, date, "11:00 AM - 01:00 PM")

        val dayBeforeCode = ReminderScheduler.rollingDayBeforeCode(fest.id)
        val morningCode = ReminderScheduler.rollingMorningCode(fest.id)

        // Day before enabled, morning of disabled
        ReminderScheduler.scheduleRollingWindow(
            context = context,
            occurrences = listOf(occ),
            dayBeforeEnabled = true,
            morningOfEnabled = false,
            muhuratStartEnabled = false
        )

        var codes = ReminderScheduler.getActiveRollingAlarmCodes(context)
        assertTrue(codes.contains(dayBeforeCode))
        assertFalse(codes.contains(morningCode))

        // Toggle: Day before disabled, morning of enabled
        ReminderScheduler.scheduleRollingWindow(
            context = context,
            occurrences = listOf(occ),
            dayBeforeEnabled = false,
            morningOfEnabled = true,
            muhuratStartEnabled = false
        )

        codes = ReminderScheduler.getActiveRollingAlarmCodes(context)
        assertFalse("Day-before must be cancelled when toggled off", codes.contains(dayBeforeCode))
        assertTrue("Morning-of must be scheduled when toggled on", codes.contains(morningCode))
    }

    @Test
    fun testScenario9_rescheduleAllRemindersIsIdempotentAndReconcilesCustomAndRolling() {
        val fest = BundledCatalog.FESTIVALS[0].toDomain()
        val date = LocalDate.now().plusDays(8)
        val occ = FestivalOccurrence(fest, date, "11:00 AM - 01:00 PM")

        val custom = CustomReminderEntry(
            festivalId = fest.id,
            festivalName = fest.nameEnglish,
            dateFormatted = date.toString(),
            triggerType = "ON_DAY",
            timeLabel = "08:00 AM"
        )

        val prefs = UserPreferences(
            dayBeforeReminder = true,
            morningOfReminder = true,
            muhuratStartReminder = false,
            customReminders = listOf(custom)
        )

        // Run 1
        ReminderScheduler.rescheduleAllReminders(context, listOf(occ), prefs)
        val rolling1 = ReminderScheduler.getActiveRollingAlarmCodes(context)
        val custom1 = ReminderScheduler.getActiveCustomAlarmCodes(context)
        assertEquals(2, rolling1.size)
        assertEquals(1, custom1.size)

        // Run 2 (Repeated execution with identical occurrences and preferences)
        ReminderScheduler.rescheduleAllReminders(context, listOf(occ), prefs)
        val rolling2 = ReminderScheduler.getActiveRollingAlarmCodes(context)
        val custom2 = ReminderScheduler.getActiveCustomAlarmCodes(context)
        assertEquals("Rolling alarms must be idempotent without duplication", rolling1, rolling2)
        assertEquals("Custom alarms must be idempotent without duplication", custom1, custom2)
    }
}
