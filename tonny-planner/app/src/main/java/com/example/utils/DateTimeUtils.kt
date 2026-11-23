package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayChip(
    val isoDate: String,
    val dayShort: String,
    val dayNumber: String,
    val monthShort: String,
    val isToday: Boolean
)

object DateTimeUtils {
    private val spanishLocale = Locale("es", "CO")
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val timeHmFormat = SimpleDateFormat("HH:mm", Locale.US)

    private val shortDays = arrayOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")
    private val shortMonths = arrayOf(
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic"
    )
    private val fullDays = arrayOf(
        "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    )

    fun todayIso(): String = synchronized(isoFormat) {
        isoFormat.format(Calendar.getInstance().time)
    }

    fun nowLocalTimeHm(): String = synchronized(timeHmFormat) {
        timeHmFormat.format(Calendar.getInstance().time)
    }

    fun nowIsoTimestamp(): String = synchronized(timestampFormat) {
        timestampFormat.format(Calendar.getInstance().time)
    }

    fun isCurrentlyMorning(): Boolean {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) < 12
    }

    fun currentMultiplier(): Double = if (isCurrentlyMorning()) 1.5 else 1.0

    fun isMorningTimestamp(isoTimestamp: String?): Boolean {
        if (isoTimestamp.isNullOrBlank()) return false
        val tIndex = isoTimestamp.indexOf('T')
        if (tIndex == -1 || tIndex + 3 > isoTimestamp.length) return false
        val hour = isoTimestamp.substring(tIndex + 1, tIndex + 3).toIntOrNull() ?: return false
        return hour < 12
    }

    fun addDays(isoDate: String, days: Int): String {
        val cal = parseIsoToCalendar(isoDate)
        cal.add(Calendar.DAY_OF_MONTH, days)
        return synchronized(isoFormat) { isoFormat.format(cal.time) }
    }

    fun normalizeTimeHm(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val match = Regex("^(\\d{1,2}):(\\d{2})$").find(raw.trim()) ?: return null
        val hour = match.groupValues[1].toIntOrNull()?.takeIf { it in 0..23 } ?: return null
        val minute = match.groupValues[2].toIntOrNull()?.takeIf { it in 0..59 } ?: return null
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }

    fun addMinutesToTimeHm(timeHm: String?, deltaMinutes: Int): String? {
        val normalized = normalizeTimeHm(timeHm) ?: return null
        val parts = normalized.split(":")
        val totalMinutes = parts[0].toInt() * 60 + parts[1].toInt() + deltaMinutes
        val wrapped = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60)
        return String.format(Locale.US, "%02d:%02d", wrapped / 60, wrapped % 60)
    }

    fun formatDisplayTime(timeHm: String?, use24Hour: Boolean): String {
        val normalized = normalizeTimeHm(timeHm) ?: return ""
        if (use24Hour) return normalized
        val parts = normalized.split(":")
        val hour24 = parts[0].toInt()
        val minute = parts[1].toInt()
        val suffix = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, suffix)
    }

    fun formatTimeRange(startTime: String?, endTime: String?, use24Hour: Boolean): String? {
        val start = normalizeTimeHm(startTime) ?: return null
        val end = normalizeTimeHm(endTime)
        return if (end != null) {
            "${formatDisplayTime(start, use24Hour)} - ${formatDisplayTime(end, use24Hour)}"
        } else {
            formatDisplayTime(start, use24Hour)
        }
    }

    fun formatReadableDate(isoDate: String): String {
        val today = todayIso()
        val tomorrow = addDays(today, 1)
        val yesterday = addDays(today, -1)
        val cal = parseIsoToCalendar(isoDate)
        val dayNum = cal.get(Calendar.DAY_OF_MONTH)
        val month = shortMonths[cal.get(Calendar.MONTH)]
        val dayName = fullDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
        return when (isoDate) {
            today -> "Hoy, $dayNum $month"
            tomorrow -> "Mañana, $dayNum $month"
            yesterday -> "Ayer, $dayNum $month"
            else -> "$dayName, $dayNum $month"
        }
    }

    fun formatMonthYear(isoDate: String): String {
        val cal = parseIsoToCalendar(isoDate)
        val monthNames = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
    }

    fun getHorizontalDateStrip(todayIso: String = todayIso()): List<DayChip> {
        return (-2..7).map { offset ->
            val date = addDays(todayIso, offset)
            toDayChip(date, todayIso)
        }
    }

    fun getWeekDays(anchorIso: String, todayIso: String = todayIso()): List<DayChip> {
        val cal = parseIsoToCalendar(anchorIso)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = when (dow) {
            Calendar.SUNDAY -> -6
            else -> Calendar.MONDAY - dow
        }
        cal.add(Calendar.DAY_OF_MONTH, diffToMonday)
        return (0..6).map { offset ->
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, offset)
            val iso = synchronized(isoFormat) { isoFormat.format(dayCal.time) }
            toDayChip(iso, todayIso)
        }
    }

    fun nextOccurrenceOfWeekday(referenceIso: String, targetCalendarDayOfWeek: Int): String {
        val cal = parseIsoToCalendar(referenceIso)
        val currentDow = cal.get(Calendar.DAY_OF_WEEK)
        var delta = (targetCalendarDayOfWeek - currentDow + 7) % 7
        if (delta == 0) delta = 7
        cal.add(Calendar.DAY_OF_MONTH, delta)
        return synchronized(isoFormat) { isoFormat.format(cal.time) }
    }

    fun parseHourOfDay(timeHm: String?): Int? {
        if (timeHm.isNullOrBlank()) return null
        val parts = timeHm.split(":")
        return parts.firstOrNull()?.toIntOrNull()?.takeIf { it in 0..23 }
    }

    private fun toDayChip(isoDate: String, todayIso: String): DayChip {
        val cal = parseIsoToCalendar(isoDate)
        val dowIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
        val month = shortMonths[cal.get(Calendar.MONTH)]
        return DayChip(
            isoDate = isoDate,
            dayShort = shortDays[dowIndex],
            dayNumber = dayNum,
            monthShort = month,
            isToday = isoDate == todayIso
        )
    }

    private fun parseIsoToCalendar(isoDate: String): Calendar {
        val cal = Calendar.getInstance(spanishLocale)
        val date = synchronized(isoFormat) {
            runCatching { isoFormat.parse(isoDate) }.getOrNull()
        }
        if (date != null) {
            cal.time = date
        }
        return cal
    }
}
