package com.example.vietnam_travel_itinerary_android.ui.itinerary

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DISPLAY_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

/** Parse chuỗi hiển thị "08:30 AM/PM" hoặc ISO "20:30:00" thành LocalTime. */
fun parseItineraryTime(value: String): LocalTime? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    return runCatching { LocalTime.parse(trimmed, DISPLAY_TIME_FORMATTER) }.getOrNull()
        ?: runCatching { LocalTime.parse(trimmed) }.getOrNull()
}

/** Format LocalTime / ISO string thành "08:30 AM" (uppercase). */
fun formatItineraryTimeForDisplay(value: String?): String {
    if (value.isNullOrBlank()) return "08:00 AM"
    val localTime = parseItineraryTime(value) ?: return "08:00 AM"
    return localTime.format(DISPLAY_TIME_FORMATTER).uppercase(Locale.US)
}

/** Chuyển input từ time picker sang ISO local time cho API (24h). */
fun itineraryInputTimeToIso(value: String): String {
    val clean = value.ifBlank { "08:00 AM" }.trim()
    return parseItineraryTime(clean)?.format(DateTimeFormatter.ISO_LOCAL_TIME) ?: "08:00:00"
}

/** Số phút từ 00:00 — dùng để sort timeline đúng thứ tự AM/PM. */
fun itineraryTimeSortMinutes(value: String): Int {
    val localTime = parseItineraryTime(value) ?: return Int.MAX_VALUE
    return localTime.hour * 60 + localTime.minute
}

fun TimelineItemData.timelineSortKey(): Int =
    itineraryTimeSortMinutes(time) * 10_000 + orderIndex
