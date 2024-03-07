package com.restaurant.services

import java.text.SimpleDateFormat
import java.util.*


class DateService {
    companion object {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

        fun parseDate(dateString: String): Date {
            return dateFormat.parse(dateString)
        }

        fun parseDate(dateSeconds: Long): Date {
            return Date(dateSeconds * 1000L)
        }

        fun formatDate(date: Date): String {
            return dateFormat.format(date)
        }

        fun addDays(date: Date, days: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, days)
            return calendar.time
        }
    }
}

fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}