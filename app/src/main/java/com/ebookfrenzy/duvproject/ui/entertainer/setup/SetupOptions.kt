package com.ebookfrenzy.duvproject.ui.entertainer.setup

import java.text.NumberFormat
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A dropdown choice: [label] is shown on screen, [value] is what the backend expects. */
data class Choice(val label: String, val value: String)

val List<Choice>.labels: List<String> get() = map { it.label }
fun List<Choice>.valueFor(label: String): String? = firstOrNull { it.label == label }?.value
fun List<Choice>.labelFor(value: String?): String? = firstOrNull { it.value == value }?.label

/** Choices for the set-up dropdowns, matched to the backend's enums. */
object SetupOptions {

    val entertainmentTypes = listOf(Choice("DJ", "DJ"), Choice("MC", "MC"), Choice("Live band", "LIVE_BAND"))

    /** Base and highest charge, in whole naira. */
    val charges: List<Long> = listOf(
        20_000, 50_000, 100_000, 150_000, 200_000, 300_000, 500_000, 750_000,
        1_000_000, 1_500_000, 2_000_000, 3_000_000, 5_000_000,
    )

    private val nairaFormat = NumberFormat.getIntegerInstance(Locale.US)
    fun naira(amount: Long): String = "₦" + nairaFormat.format(amount)
    val chargeLabels: List<String> get() = charges.map(::naira)
    /** "₦150,000" → 150000 (also accepts amounts not in [charges], e.g. set before the list existed). */
    fun chargeFrom(label: String): Long? = label.filter(Char::isDigit).toLongOrNull()

    const val YES = "Yes"
    const val NO = "No"
    val yesNo = listOf(YES, NO)

    val years: List<String> get() = (Year.now().value downTo 1970).map(Int::toString)

    val eventTypes = listOf(
        Choice("Wedding", "WEDDING"), Choice("Birthday", "BIRTHDAY"), Choice("Corporate", "CORPORATE"),
        Choice("Club", "CLUB"), Choice("Concert", "CONCERT"), Choice("Party", "PARTY"),
        Choice("Religious", "RELIGIOUS"), Choice("Other", "OTHER"),
    )

    val relationships = listOf("Parent", "Sibling", "Spouse", "Child", "Relative", "Friend", "Colleague", "Manager", "Other")

    val idTypes = listOf(
        Choice("National ID card", "NATIONAL_ID"), Choice("NIN slip", "NIN_SLIP"),
        Choice("International passport", "INTERNATIONAL_PASSPORT"), Choice("Driver's licence", "DRIVERS_LICENSE"),
        Choice("Voter's card", "VOTERS_CARD"),
    )

    /** Dates are shown as dd/MM/yyyy and sent as ISO yyyy-MM-dd. */
    val uiDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    fun parseUiDate(text: String): LocalDate? = runCatching { LocalDate.parse(text.trim(), uiDate) }.getOrNull()
    fun isoToUi(iso: String?): String? = iso?.let { runCatching { LocalDate.parse(it).format(uiDate) }.getOrNull() }

    /** Joins/splits the multi-choice "Available for" field. */
    const val SEPARATOR = ", "
}
