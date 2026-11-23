package com.example

import com.example.data.remote.VoiceScheduleParser
import com.example.data.repository.GamificationEngine
import com.example.model.ItemType
import com.example.model.VoiceParsePayload
import com.example.utils.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TonnyPlannerUnitTest {

    @Test
    fun xpCalculation_appliesMorningMultiplierBeforeNoon() {
        val (morningXp, morningMult) = GamificationEngine.calculateAwardedXp(isMorning = true)
        assertEquals(15, morningXp)
        assertEquals(1.5, morningMult, 0.001)

        val (baseXp, baseMult) = GamificationEngine.calculateAwardedXp(isMorning = false)
        assertEquals(10, baseXp)
        assertEquals(1.0, baseMult, 0.001)
    }

    @Test
    fun levelProgression_rollsOverExcessXpAndSupportsExactReversal() {
        val at95 = GamificationEngine.computeLevelProgress(95)
        assertEquals(1, at95.level)
        assertEquals(95, at95.currentLevelXp)
        assertEquals(100, at95.nextLevelXpRequired)

        val at110 = GamificationEngine.computeLevelProgress(110)
        assertEquals(2, at110.level)
        assertEquals(10, at110.currentLevelXp)
        assertEquals(200, at110.nextLevelXpRequired)

        val at325 = GamificationEngine.computeLevelProgress(325)
        assertEquals(3, at325.level)
        assertEquals(25, at325.currentLevelXp)
        assertEquals(300, at325.nextLevelXpRequired)

        val reverted = GamificationEngine.computeLevelProgress(110 - 15)
        assertEquals(1, reverted.level)
        assertEquals(95, reverted.currentLevelXp)
    }

    @Test
    fun voiceParser_extractsHabitDateStartAndSuggestedEndTime() {
        val today = DateTimeUtils.todayIso()
        val tomorrow = DateTimeUtils.addDays(today, 1)

        val habitResult = VoiceScheduleParser.parse(
            VoiceParsePayload(
                rawTranscript = "Meditar 10 minutos cada mañana a las 7",
                referenceDate = today,
                userLocalTime = "10:00"
            )
        )
        assertEquals(ItemType.HABIT, habitResult.suggestedType)
        assertEquals(today, habitResult.suggestedDate)
        assertEquals("07:00", habitResult.suggestedStartTime)
        assertEquals("08:00", habitResult.suggestedEndTime)
        assertEquals("Meditar 10 minutos", habitResult.suggestedTitle)

        val taskResult = VoiceScheduleParser.parse(
            VoiceParsePayload(
                rawTranscript = "Enviar reporte del proyecto mañana a las 3 pm",
                referenceDate = today,
                userLocalTime = "10:00"
            )
        )
        assertEquals(ItemType.TASK, taskResult.suggestedType)
        assertEquals(tomorrow, taskResult.suggestedDate)
        assertEquals("15:00", taskResult.suggestedStartTime)
        assertEquals("16:00", taskResult.suggestedEndTime)
        assertEquals("Enviar reporte del proyecto", taskResult.suggestedTitle)

        val rangeResult = VoiceScheduleParser.parse(
            VoiceParsePayload(
                rawTranscript = "Leer 20 páginas hoy de 20:00 a 21:00",
                referenceDate = today,
                userLocalTime = "14:00"
            )
        )
        assertEquals(ItemType.TASK, rangeResult.suggestedType)
        assertEquals(today, rangeResult.suggestedDate)
        assertEquals("20:00", rangeResult.suggestedStartTime)
        assertEquals("21:00", rangeResult.suggestedEndTime)
        assertEquals("Leer 20 páginas", rangeResult.suggestedTitle)

        val noTimeResult = VoiceScheduleParser.parse(
            VoiceParsePayload(
                rawTranscript = "Comprar café",
                referenceDate = today,
                userLocalTime = "14:00"
            )
        )
        assertEquals(ItemType.TASK, noTimeResult.suggestedType)
        assertEquals(today, noTimeResult.suggestedDate)
        assertNull(noTimeResult.suggestedStartTime)
        assertNull(noTimeResult.suggestedEndTime)
        assertEquals("Comprar café", noTimeResult.suggestedTitle)
    }

    @Test
    fun timeRangeHelpers_format24hAnd12hCorrectly() {
        assertEquals("10:30", DateTimeUtils.addMinutesToTimeHm("09:30", 60))
        assertEquals("09:30 - 10:30", DateTimeUtils.formatTimeRange("09:30", "10:30", use24Hour = true))
        assertEquals("2:30 PM - 3:30 PM", DateTimeUtils.formatTimeRange("14:30", "15:30", use24Hour = false))
    }
}
