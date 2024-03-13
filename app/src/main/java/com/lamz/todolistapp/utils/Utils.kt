package com.lamz.todolistapp.utils

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import java.util.Locale

object Utils {
    fun getCurrentTimeWithFormat(): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }
}