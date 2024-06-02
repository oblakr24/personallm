package util

import feature.commonui.calendar.YearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import kotlinx.datetime.toLocalDateTime

fun Instant.formatted() = formatInstant(this)

private fun formatInstant(instant: Instant): String {
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = instant.toLocalDateTime(timeZone)
    return localDateTime.format(DateFormats.dateTime)
}

fun YearMonth.format(): String {
    val month = DateFormats.monthAbbreviations[month.ordinal]
    return "$month, $year"
}

object DateFormats {

    private const val FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm"

    val monthAbbreviations = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    @OptIn(FormatStringsInDatetimeFormats::class)
    val dateTime = LocalDateTime.Format {
        byUnicodePattern(FORMAT_PATTERN)
    }

    val custom = LocalDateTime.Format {
        monthNumber();
        char('/');
        dayOfMonth()
        char(' ')
        hour(); char(':'); minute()
        optional { char(':'); second() }
    }
}
