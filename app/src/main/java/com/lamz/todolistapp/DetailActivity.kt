package com.lamz.todolistapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lamz.todolistapp.data.InputTodo
import com.lamz.todolistapp.data.TodoItem
import com.lamz.todolistapp.databinding.ActivityDetailBinding
import com.lamz.todolistapp.databinding.AlertDialogBinding
import com.lamz.todolistapp.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private lateinit var todoId: String
    private var _alertDialogBinding: AlertDialogBinding? = null
    private val alertDialogBinding get() = _alertDialogBinding
    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
        }

        todoId = intent.getStringExtra(toodoId) ?: ""
        lifecycleScope.launch(Dispatchers.IO) {
            database = Utils.firebaseDatabaseTodo
            val taskRef = database.child(todoId)
            taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val todo = snapshot.getValue(TodoItem::class.java)
                        if (isDialogShown) {
                            showAlertDialog(todo?.title, todo?.detail, todo?.status, todo?.completed, todo?.uid_completed)
                        }
                        with(binding) {
                            titleDetail.text = todo?.title
                            bannerDetail.detailDate.text = getString(R.string.date, todo?.time)
                            bannerDetail.detailTodo.text = todo?.detail
                            bannerDetail.status.text = getString(R.string.status, todo?.status)
                            btnBack.setOnClickListener {
                                finish()
                            }
                            bannerDetail.btnEdit.setOnClickListener {
                                showAlertDialog(todo?.title, todo?.detail, todo?.status, todo?.completed, todo?.uid_completed)
                            }

                            btnDelete.setOnClickListener {
                                deleteTodo()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogShown", isDialogShown)
    }

    private fun showAlertDialog(title : String? , detail : String?, status : String?, isComplete : String?,uidCompleted : String?) {
        isDialogShown = true
        val builder = AlertDialog.Builder(this)
        _alertDialogBinding = AlertDialogBinding.inflate(layoutInflater)
        val view = alertDialogBinding?.root
        alertDialogBinding?.apply {
            val todo = titleInput
            val detailTodo = taskInput

            todo.setText(title)
            detailTodo.setText(detail)

            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_bg)
            dialog.show()

            cancel.setOnClickListener {
                dialog.dismiss()
                isDialogShown = false
            }

            btnSave.setOnClickListener {
                updateTodo(todo.text.toString(), detailTodo.text.toString(),status, isComplete, uidCompleted,dialog )
            }
        }
    }



    private fun updateTodo(todo : String, detail : String,status : String?, isComplete : String?,uidCompleted : String?, finish : AlertDialog){
        lifecycleScope.launch(Dispatchers.IO) {
            database = Utils.firebaseDatabaseTodo
            val taskId = database.push().key!!
            val uid = auth.currentUser?.uid
            val currentTime = Utils.getCurrentTimeWithFormat()
            val updateTask = InputTodo(taskId,uid!!,todo, detail, time = currentTime, status = status!!, isCompleted = isComplete!!, uid_completed = uidCompleted!!)

            val dbRef = database.child(todoId)
            dbRef.setValue(updateTask)
                .addOnSuccessListener {
                    Toast.makeText(this@DetailActivity, "Succes Update Todo", Toast.LENGTH_SHORT).show()
                    finish.dismiss()
                    finish()
                }
        }

    }

    private fun deleteTodo() {
        lifecycleScope.launch(Dispatchers.IO) {
            val todoRef = database.child(todoId)
            todoRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this@DetailActivity, "Todo deleted successfully", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    val intent = Intent(this@DetailActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@DetailActivity, "Failed to delete todo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    companion object {
        const val toodoId = "TodoId"
    }
}