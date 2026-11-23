package com.example.data.remote

import com.example.model.ItemType
import com.example.model.VoiceParsePayload
import com.example.model.VoiceParseResult
import com.example.utils.DateTimeUtils
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

/**
 * Lightweight natural language parser for Spanish voice commands.
 * Extracts title, type, scheduledDate, startTime, and auto-suggests endTime (+1h).
 */
object VoiceScheduleParser {

    private val habitTriggers = listOf(
        "todos los dias",
        "cada dia",
        "cada manana",
        "todas las mananas",
        "cada noche",
        "todas las noches",
        "diariamente",
        "diario",
        "habito"
    )

    private val weekdayMap = mapOf(
        "lunes" to Calendar.MONDAY,
        "martes" to Calendar.TUESDAY,
        "miercoles" to Calendar.WEDNESDAY,
        "jueves" to Calendar.THURSDAY,
        "viernes" to Calendar.FRIDAY,
        "sabado" to Calendar.SATURDAY,
        "domingo" to Calendar.SUNDAY
    )

    fun parse(payload: VoiceParsePayload): VoiceParseResult {
        val raw = payload.rawTranscript.trim()
        if (raw.isEmpty()) {
            return VoiceParseResult(
                suggestedTitle = "",
                suggestedDate = payload.referenceDate,
                suggestedStartTime = null,
                suggestedEndTime = null,
                suggestedType = ItemType.TASK
            )
        }

        val normalized = stripAccents(raw.lowercase(Locale.ROOT))

        // 1. Detect type (habit vs task)
        val isHabit = habitTriggers.any { normalized.contains(it) }
        val suggestedType = if (isHabit) ItemType.HABIT else ItemType.TASK

        // 2. Detect date
        var suggestedDate = payload.referenceDate
        var matchedDatePhrase: Regex? = null

        when {
            normalized.contains("pasado manana") -> {
                suggestedDate = DateTimeUtils.addDays(DateTimeUtils.todayIso(), 2)
                matchedDatePhrase = Regex("\\b(para\\s+)?pasado\\s+ma[nñ]ana\\b", RegexOption.IGNORE_CASE)
            }
            Regex("\\bmanana\\b").containsMatchIn(normalized) &&
                !normalized.contains("cada manana") &&
                !normalized.contains("de la manana") &&
                !normalized.contains("por la manana") -> {
                suggestedDate = DateTimeUtils.addDays(DateTimeUtils.todayIso(), 1)
                matchedDatePhrase = Regex("\\b(para\\s+)?ma[nñ]ana\\b", RegexOption.IGNORE_CASE)
            }
            Regex("\\bhoy\\b").containsMatchIn(normalized) -> {
                suggestedDate = DateTimeUtils.todayIso()
                matchedDatePhrase = Regex("\\b(para\\s+)?hoy\\b", RegexOption.IGNORE_CASE)
            }
            else -> {
                for ((dayWord, calendarDow) in weekdayMap) {
                    val dayRegex = Regex("\\b((el\\s+)?(proximo|este)\\s+)?$dayWord\\b", RegexOption.IGNORE_CASE)
                    if (dayRegex.containsMatchIn(normalized)) {
                        suggestedDate = DateTimeUtils.nextOccurrenceOfWeekday(
                            DateTimeUtils.todayIso(),
                            calendarDow
                        )
                        matchedDatePhrase = Regex(
                            "\\b((para\\s+)?(el\\s+)?(pr[oó]ximo|este)\\s+)?${weekdayAccentRegex(dayWord)}\\b",
                            RegexOption.IGNORE_CASE
                        )
                        break
                    }
                }
            }
        }

        // 3. Detect startTime + optional endTime
        val rangeRegex = Regex(
            "(?:de|entre)\\s+(?:las?\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s+(?:a|hasta)\\s+(?:las?\\s+)?(\\d{1,2})(?::(\\d{2}))?(?:\\s*(am|pm|a\\.m\\.|p\\.m\\.|de\\s+la\\s+ma[nñ]ana|de\\s+la\\s+tarde|de\\s+la\\s+noche))?",
            RegexOption.IGNORE_CASE
        )
        val rangeMatch = rangeRegex.find(raw)

        val singleTimeRegex = Regex(
            "(?:a\\s+las?|alas)\\s+(\\d{1,2})(?::(\\d{2}))?(?:\\s*(am|pm|a\\.m\\.|p\\.m\\.|de\\s+la\\s+ma[nñ]ana|por\\s+la\\s+ma[nñ]ana|de\\s+la\\s+tarde|por\\s+la\\s+tarde|de\\s+la\\s+noche|por\\s+la\\s+noche|del\\s+mediod[ií]a))?",
            RegexOption.IGNORE_CASE
        )
        val singleMatch = if (rangeMatch == null) singleTimeRegex.find(raw) else null

        var suggestedStartTime: String? = null
        var suggestedEndTime: String? = null
        var matchedTimeText: String? = null

        if (rangeMatch != null) {
            val h1 = rangeMatch.groupValues[1].toIntOrNull()
            val m1 = rangeMatch.groupValues[2].toIntOrNull() ?: 0
            val h2 = rangeMatch.groupValues[3].toIntOrNull()
            val m2 = rangeMatch.groupValues[4].toIntOrNull() ?: 0
            val suffix = stripAccents(rangeMatch.groupValues[5].lowercase(Locale.ROOT))
            suggestedStartTime = format24h(h1, m1, suffix)
            suggestedEndTime = format24h(h2, m2, suffix)
            matchedTimeText = rangeMatch.value
        } else if (singleMatch != null) {
            val rawHour = singleMatch.groupValues[1].toIntOrNull()
            val rawMinute = singleMatch.groupValues[2].toIntOrNull() ?: 0
            val suffix = stripAccents(singleMatch.groupValues[3].lowercase(Locale.ROOT))
            suggestedStartTime = format24h(rawHour, rawMinute, suffix)
            suggestedEndTime = DateTimeUtils.addMinutesToTimeHm(suggestedStartTime, 60)
            matchedTimeText = singleMatch.value
        }

        // 4. Clean title
        var cleanedTitle = raw
        if (matchedTimeText != null) {
            cleanedTitle = cleanedTitle.replace(matchedTimeText, " ")
        }
        if (matchedDatePhrase != null) {
            cleanedTitle = cleanedTitle.replace(matchedDatePhrase, " ")
        }

        val frequencyCleanupRegexes = listOf(
            Regex("\\b(como\\s+)?h[aá]bito\\b", RegexOption.IGNORE_CASE),
            Regex("\\btodos\\s+los\\s+d[ií]as\\b", RegexOption.IGNORE_CASE),
            Regex("\\bcada\\s+(d[ií]a|ma[nñ]ana|noche)\\b", RegexOption.IGNORE_CASE),
            Regex("\\btodas\\s+las\\s+(ma[nñ]anas|noches)\\b", RegexOption.IGNORE_CASE),
            Regex("\\bdiariamente\\b|\\bdiario\\b", RegexOption.IGNORE_CASE),
            Regex("^(crear|agendar|programar|recordar|anotar|hacer)\\s+(una\\s+tarea\\s+de\\s+|un\\s+h[aá]bito\\s+de\\s+|tarea\\s+|para\\s+)?", RegexOption.IGNORE_CASE)
        )
        frequencyCleanupRegexes.forEach { regex ->
            cleanedTitle = cleanedTitle.replace(regex, " ")
        }

        cleanedTitle = cleanedTitle
            .replace(Regex("\\s+"), " ")
            .trim(' ', ',', '.', ';', ':', '-')
            .replace(Regex("^(para|el|la|de|a)\\s+", RegexOption.IGNORE_CASE), "")
            .trim()

        if (cleanedTitle.isEmpty()) {
            cleanedTitle = raw
        } else {
            cleanedTitle = cleanedTitle.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString()
            }
        }

        return VoiceParseResult(
            suggestedTitle = cleanedTitle,
            suggestedDate = suggestedDate,
            suggestedStartTime = suggestedStartTime,
            suggestedEndTime = suggestedEndTime,
            suggestedType = suggestedType
        )
    }

    private fun format24h(rawHour: Int?, rawMinute: Int, suffix: String): String? {
        if (rawHour == null || rawHour !in 0..23 || rawMinute !in 0..59) return null
        var hour24 = rawHour
        when {
            suffix.contains("pm") || suffix.contains("p.m") || suffix.contains("tarde") || suffix.contains("noche") -> {
                if (hour24 in 1..11) hour24 += 12
            }
            suffix.contains("am") || suffix.contains("a.m") || suffix.contains("manana") -> {
                if (hour24 == 12) hour24 = 0
            }
        }
        return String.format(Locale.US, "%02d:%02d", hour24, rawMinute)
    }

    private fun weekdayAccentRegex(dayWord: String): String = when (dayWord) {
        "miercoles" -> "mi[eé]rcoles"
        "sabado" -> "s[aá]bado"
        else -> dayWord
    }

    private fun stripAccents(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
