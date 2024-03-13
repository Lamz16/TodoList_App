package com.lamz.todolistapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoItem(
    var todoId : String,
    val title : String,
    val detail : String,
    var completed : String,
    val time : String,
    val status : String,
    val uid_completed : String
) : Parcelable{
    constructor() :this("","", "","","","","")
}
