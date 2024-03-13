package com.lamz.todolistapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InputTodo(
    val todoId : String,
    val uid: String,
    val title : String,
    val detail : String,
    val isCompleted : String = "no",
    val uid_completed : String ="",
    val status : String = "Incomplete",
    val time : String
) : Parcelable
