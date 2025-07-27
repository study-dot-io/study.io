package com.example.studyio.utils

import java.text.SimpleDateFormat
import java.util.*

object StudyScheduleUtils {

    private val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    /**
     * Creates a bitmask from a list of selected day indices (0=Sunday, 6=Saturday).
     */
    fun createScheduleBitmask(selectedDays: List<Int>): Int {
        var bitmask = 0
        selectedDays.forEach { dayIndex ->
            if (dayIndex in 0..6) {
                bitmask = bitmask or (1 shl dayIndex)
            }
        }
        return bitmask
    }

    /**
     * Gets a list of day indices from a bitmask.
     */
    fun getDayIndicesFromBitmask(bitmask: Int): List<Int> {
        val selectedDays = mutableListOf<Int>()
        for (i in 0..6) {
            if ((bitmask and (1 shl i)) > 0) {
                selectedDays.add(i)
            }
        }
        return selectedDays
    }

    /**
     * Formats a schedule bitmask into a human-readable string (e.g., "MWF").
     */
    fun formatSchedule(bitmask: Int): String {
        if (bitmask == 0) return "Not scheduled"
        if (bitmask == 127) return "Every day"

        val dayIndices = getDayIndicesFromBitmask(bitmask)
        
        // Handle common patterns
        val isWeekday = dayIndices.containsAll(listOf(1, 2, 3, 4, 5)) && dayIndices.size == 5
        val isWeekend = dayIndices.containsAll(listOf(0, 6)) && dayIndices.size == 2
        if (isWeekday) return "Weekdays"
        if (isWeekend) return "Weekends"

        return dayIndices.joinToString(", ") { daysOfWeek[it] }
    }

    /**
     * Checks if a deck is scheduled for study today based on its bitmask.
     */
    fun isScheduledToday(bitmask: Int): Boolean {
        val calendar = Calendar.getInstance()
        val todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.SUNDAY is 1, so we get 0-6
        return (bitmask and (1 shl todayIndex)) > 0
    }

    /**
     * Gets the next scheduled date as a formatted string.
     */
    fun getNextScheduledDate(schedule: Int): String {
        if (schedule == 0) return ""

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Convert to 0-6

        for (i in 1..7) {
            val checkDay = (currentDay + i) % 7
            if ((schedule and (1 shl checkDay)) > 0) {
                calendar.add(Calendar.DAY_OF_MONTH, i)
                return SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(calendar.time)
            }
        }
        return "Next week" // Should not happen if schedule is not 0
    }
}

