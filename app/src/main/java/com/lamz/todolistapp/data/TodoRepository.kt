package com.lamz.todolistapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.lamz.todolistapp.utils.Utils

class TodoRepository {

    private val database: DatabaseReference = Utils.firebaseDatabase.getReference(Utils.TODO)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getTodoList(callback: (ArrayList<TodoItem>) -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        val query: Query = database.orderByChild("uid").equalTo(uid)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = arrayListOf<TodoItem>()
                if (snapshot.exists()) {
                    for (todoSnap in snapshot.children) {
                        val todoData = todoSnap.getValue(TodoItem::class.java)
                        todoData?.let {
                            it.todoId = todoSnap.key ?: ""
                            tempList.add(it)
                        }
                    }
                }
                callback(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    fun getCompleteTodoList(callback: (ArrayList<TodoItem>) -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val uidCompleted = "${uid}_yes"

        val query: Query = database.orderByChild(Utils.UID_COMPLETED).equalTo(uidCompleted)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = arrayListOf<TodoItem>()
                if (snapshot.exists()) {
                    for (todoSnap in snapshot.children) {
                        val todoData = todoSnap.getValue(TodoItem::class.java)
                        todoData?.let {
                            it.todoId = todoSnap.key ?: ""
                            tempList.add(it)
                        }
                    }
                }
                callback(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    fun createTodo(todo: String, detail: String) {
        val taskId = database.push().key!!
        val uid = auth.currentUser?.uid
        val currentTime = Utils.getCurrentTimeWithFormat()
        val newTodo = InputTodo(taskId, uid ?: "", todo, detail, time = currentTime)

        database.child(taskId).setValue(newTodo)
    }

}

