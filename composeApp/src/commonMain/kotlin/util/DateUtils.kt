package util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

fun Instant.formatted(format: DateTimeFormat<LocalDateTime>) = formatInstant(this, format)
fun Instant.formattedReadable() = formatInstant(this, DateFormats.readable)
fun Instant.formatTimeElapsed() = formatTimeElapsedSince(this)

private fun formatInstant(instant: Instant, format: DateTimeFormat<LocalDateTime>): String {
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = instant.toLocalDateTime(timeZone)
    return localDateTime.format(format)
}

private fun formatTimeElapsedSince(since: Instant): String {
    val elapsed = Clock.System.now().minus(since)
    val wholeDays = elapsed.inWholeDays
    val wholeHours = elapsed.inWholeHours
    if (wholeDays > 0) return "${wholeDays}d, ${wholeHours.mod(24)}h ago"
    val wholeMins = elapsed.inWholeMinutes
    if (wholeHours > 0) return "${wholeHours}h ${wholeMins.mod(60)}min ago"
    if (wholeMins > 0) return "${wholeMins}min ago"
    return "Just now"
}

object DateFormats {

    private const val FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm"

    @OptIn(FormatStringsInDatetimeFormats::class)
    val dateTime = LocalDateTime.Format {
        byUnicodePattern(FORMAT_PATTERN)
    }

    val readable = LocalDateTime.Format {
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
        char(',')
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        dayOfMonth(Padding.NONE)
        char(',')
        char(' ')
        amPmHour(Padding.NONE)
        char(':')
        minute(Padding.NONE)
        amPmMarker(am = "AM", pm = "PM")
        char(',')
        char(' ')
        year(Padding.NONE)
    }
}
