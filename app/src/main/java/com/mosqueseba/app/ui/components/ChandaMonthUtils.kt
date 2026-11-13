package com.mosqueseba.app.ui.components

import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity

data class BillingMonthItem(
    val label: String,
    val shortLabel: String,
    val yearLabel: String,
    val monthIndex: Int, // 0..11
    val year: Int,
    val isAdvance: Boolean = false
)

object ChandaMonthUtils {

    val bengaliMonths = listOf(
        "জানুয়ারি",
        "ফেব্রুয়ারি",
        "মার্চ",
        "এপ্রিল",
        "মে",
        "জুন",
        "জুলাই",
        "আগস্ট",
        "সেপ্টেম্বর",
        "অক্টোবর",
        "নভেম্বর",
        "ডিসেম্বর"
    )

    private val shortMonths = listOf(
        "জানু",
        "ফেব্রু",
        "মার্চ",
        "এপ্রিল",
        "মে",
        "জুন",
        "জুলাই",
        "আগস্ট",
        "সেপ্টে",
        "অক্টো",
        "নভে",
        "ডিসে"
    )

    private val normalizedMonthMap: Map<String, Int> by lazy {
        buildMap {
            bengaliMonths.forEachIndexed { idx, name ->
                put(normalizeToken(name), idx)
            }
            // Common spelling / English aliases
            put(normalizeToken("জানুয়ারী"), 0)
            put(normalizeToken("ফেব্রুয়ারী"), 1)
            put("january", 0)
            put("february", 1)
            put("march", 2)
            put("april", 3)
            put("may", 4)
            put("june", 5)
            put("july", 6)
            put("august", 7)
            put("september", 8)
            put("october", 9)
            put("november", 10)
            put("december", 11)
        }
    }

    fun normalizeToken(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input.trim().lowercase()) {
            when (ch) {
                '০' -> sb.append('0')
                '১' -> sb.append('1')
                '২' -> sb.append('2')
                '৩' -> sb.append('3')
                '৪' -> sb.append('4')
                '৫' -> sb.append('5')
                '৬' -> sb.append('6')
                '৭' -> sb.append('7')
                '৮' -> sb.append('8')
                '৯' -> sb.append('9')
                // Normalize decomposed য + ় (\u09AF\u09BC) vs precomposed য় (\u09DF)
                '\u09DF' -> sb.append("\u09AF\u09BC")
                else -> sb.append(ch)
            }
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    fun toBengaliNumber(number: Int): String {
        val digits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return number.toString().map { ch ->
            if (ch in '0'..'9') digits[ch - '0'] else ch
        }.joinToString("")
    }

    fun formatMonthYear(monthIndex: Int, year: Int): String {
        val safeIdx = ((monthIndex % 12) + 12) % 12
        return "${bengaliMonths[safeIdx]} ${toBengaliNumber(year)}"
    }

    fun parseMonthYear(label: String): Pair<Int, Int>? {
        val norm = normalizeToken(label)
        val yearMatch = Regex("(20\\d{2})").find(norm) ?: return null
        val year = yearMatch.groupValues[1].toIntOrNull() ?: return null
        val monthIdx = findMonthIndex(norm) ?: return null
        return monthIdx to year
    }

    private fun findMonthIndex(normalizedText: String): Int? {
        return normalizedMonthMap.entries.firstOrNull { (key, _) ->
            normalizedText.contains(key)
        }?.value
    }

    /**
     * Extracts a clean single anchor month (e.g. "সেপ্টেম্বর ২০২৪") even if the latest payment
     * spans a multi-month range like "এপ্রিল – সেপ্টেম্বর ২০২৪ (৬ মাস)".
     */
    fun resolveLatestSingleMonth(
        paymentMonths: List<String>,
        fallback: String = "সেপ্টেম্বর ২০২৪"
    ): String {
        for (raw in paymentMonths) {
            val cleaned = raw.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()
            val sep = listOf("–", "—", " থেকে ", " to ", "-", ",").firstOrNull { cleaned.contains(it) }
            val candidate = if (sep != null) {
                cleaned.split(sep).lastOrNull()?.trim().orEmpty()
            } else {
                cleaned
            }
            val parsed = parseMonthYear(candidate) ?: parseMonthYear(cleaned)
            if (parsed != null) {
                return formatMonthYear(parsed.first, parsed.second)
            }
        }
        val fallbackParsed = parseMonthYear(fallback)
        return if (fallbackParsed != null) {
            formatMonthYear(fallbackParsed.first, fallbackParsed.second)
        } else {
            "সেপ্টেম্বর ২০২৪"
        }
    }

    /**
     * Generates an ordered list of billing months: [pastMonthsCount] past months,
     * the anchor month, and [advanceMonthsCount] future/advance months.
     * Default (11 past + 1 current + 3 advance = 15 months) fits cleanly in a 3x5 grid.
     */
    fun getBillingMonthsWindow(
        anchorLabel: String = "সেপ্টেম্বর ২০২৪",
        pastMonthsCount: Int = 11,
        advanceMonthsCount: Int = 3
    ): List<BillingMonthItem> {
        val (anchorMonthIdx, anchorYear) = parseMonthYear(anchorLabel) ?: (8 to 2024)
        val anchorSerial = anchorYear * 12 + anchorMonthIdx
        val result = mutableListOf<BillingMonthItem>()

        for (offset in -pastMonthsCount..advanceMonthsCount) {
            val serial = anchorSerial + offset
            val year = serial.floorDiv(12)
            val mIdx = serial.mod(12)
            val label = formatMonthYear(mIdx, year)
            result.add(
                BillingMonthItem(
                    label = label,
                    shortLabel = shortMonths[mIdx],
                    yearLabel = toBengaliNumber(year),
                    monthIndex = mIdx,
                    year = year,
                    isAdvance = offset > 0
                )
            )
        }
        return result
    }

    /**
     * Parses a stored payment month string into individual month labels from [availableMonths].
     * Supports:
     * - Single month: "সেপ্টেম্বর ২০২৪"
     * - Contiguous range: "এপ্রিল – সেপ্টেম্বর ২০২৪ (৬ মাস)" or "নভেম্বর ২০২৩ – মে ২০২৪ (৭ মাস)"
     * - Comma-separated months: "জুলাই ২০২৪, সেপ্টেম্বর ২০২৪"
     */
    fun extractCoveredMonths(
        storedMonthText: String,
        availableMonths: List<BillingMonthItem>
    ): Set<String> {
        val trimmed = storedMonthText.trim()
        if (trimmed.isEmpty()) return emptySet()

        // Strip trailing count tag like " (৬ মাস)" before parsing ranges/lists
        val withoutCountSuffix = trimmed.replace(Regex("\\s*\\([^)]*মাস[^)]*\\)\\s*$"), "").trim()
        val normStored = normalizeToken(withoutCountSuffix)

        // 1. Direct single-month exact match
        availableMonths.firstOrNull { normalizeToken(it.label) == normStored }?.let {
            return setOf(it.label)
        }

        // 2. Range notation: "মাস১ – মাস২ ২০২৪" or "মাস১ ২০২৩ – মাস২ ২০২৪" or "থেকে"
        val rangeDelimiters = listOf("–", "—", " থেকে ", " to ", "-")
        val matchedDelimiter = rangeDelimiters.firstOrNull { withoutCountSuffix.contains(it) }
        if (matchedDelimiter != null) {
            val parts = withoutCountSuffix.split(matchedDelimiter, limit = 2).map { it.trim() }
            if (parts.size == 2) {
                val endParsed = parseMonthYear(parts[1])
                val startParsed = parseMonthYear(parts[0]) ?: endParsed?.let { (_, endYear) ->
                    val startMonthIdx = findMonthIndex(normalizeToken(parts[0]))
                    startMonthIdx?.let { it to endYear }
                }
                if (startParsed != null && endParsed != null) {
                    val startSerial = startParsed.second * 12 + startParsed.first
                    val endSerial = endParsed.second * 12 + endParsed.first
                    val minSerial = minOf(startSerial, endSerial)
                    val maxSerial = maxOf(startSerial, endSerial)
                    val matched = availableMonths.filter { item ->
                        val serial = item.year * 12 + item.monthIndex
                        serial in minSerial..maxSerial
                    }.map { it.label }.toSet()
                    if (matched.isNotEmpty()) return matched
                }
            }
        }

        // 3. Comma-separated list: "এপ্রিল ২০২৪, জুন ২০২৪, জুলাই ২০২৪"
        if (withoutCountSuffix.contains(",")) {
            val tokens = withoutCountSuffix.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val fallbackYear = parseMonthYear(withoutCountSuffix)?.second
            val matched = mutableSetOf<String>()
            tokens.forEach { token ->
                val normToken = normalizeToken(token)
                val parsed = parseMonthYear(token) ?: fallbackYear?.let { yr ->
                    findMonthIndex(normToken)?.let { idx -> idx to yr }
                }
                if (parsed != null) {
                    val targetLabel = formatMonthYear(parsed.first, parsed.second)
                    availableMonths.firstOrNull {
                        normalizeToken(it.label) == normalizeToken(targetLabel)
                    }?.let { matched.add(it.label) }
                }
            }
            if (matched.isNotEmpty()) return matched
        }

        // 4. Fallback substring check
        return availableMonths.filter { item ->
            normStored.contains(normalizeToken(item.label))
        }.map { it.label }.toSet()
    }

    /**
     * Computes pro-rata paid amount attributed to [targetMonth] from [payment].
     */
    fun amountForMonth(payment: ChandaPaymentEntity, targetMonth: String): Double {
        val normTarget = normalizeToken(targetMonth)
        val normPayment = normalizeToken(payment.month)
        if (normPayment == normTarget) return payment.amountPaid

        val window = getBillingMonthsWindow(anchorLabel = targetMonth, pastMonthsCount = 24, advanceMonthsCount = 12)
        val covered = extractCoveredMonths(payment.month, window)
        if (covered.isEmpty()) return 0.0
        val coversTarget = covered.any { normalizeToken(it) == normTarget }
        return if (coversTarget) payment.amountPaid / covered.size else 0.0
    }

    /**
     * Formats 1, contiguous N, or non-contiguous N selected months cleanly for DB & Receipt display.
     * Ensures [extractCoveredMonths] can round-trip all selected months losslessly.
     */
    fun formatMonthsForStorage(
        selectedLabels: Set<String>,
        orderedWindow: List<BillingMonthItem>
    ): String {
        val selectedIndices = orderedWindow.mapIndexedNotNull { index, item ->
            if (item.label in selectedLabels) index else null
        }
        if (selectedIndices.isEmpty()) return ""
        if (selectedIndices.size == 1) return orderedWindow[selectedIndices.first()].label

        val isContiguous = selectedIndices.zipWithNext().all { (a, b) -> b == a + 1 }
        val countBn = toBengaliNumber(selectedIndices.size)

        return if (isContiguous) {
            val first = orderedWindow[selectedIndices.first()]
            val last = orderedWindow[selectedIndices.last()]
            if (first.year == last.year) {
                "${bengaliMonths[first.monthIndex]} – ${bengaliMonths[last.monthIndex]} ${first.yearLabel} ($countBn মাস)"
            } else {
                "${first.label} – ${last.label} ($countBn মাস)"
            }
        } else {
            val labels = selectedIndices.map { orderedWindow[it].label }
            if (labels.size <= 2) {
                labels.joinToString(", ")
            } else {
                "${labels.joinToString(", ")} ($countBn মাস)"
            }
        }
    }
}
