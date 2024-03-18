package com.lamz.todolistapp.utils

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

object Utils {
    fun getCurrentTimeWithFormat(): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }

    val firebaseDatabaseTodo = FirebaseDatabase.getInstance("https://todolist-app-e056a-default-rtdb.firebaseio.com").getReference("todo")
    const val COMPLETED = "completed"
    const val UID_COMPLETED = "uid_completed"
    const val STATUS = "status"
    const val TODO = "todo"
}