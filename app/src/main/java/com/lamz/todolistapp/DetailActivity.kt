package com.lamz.todolistapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lamz.todolistapp.data.InputTodo
import com.lamz.todolistapp.data.TodoItem
import com.lamz.todolistapp.databinding.ActivityDetailBinding
import com.lamz.todolistapp.ui.home.HomeFragment
import com.lamz.todolistapp.utils.Utils

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private lateinit var todoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val color = ContextCompat.getColor(this, R.color.color_2)
        binding.container.setBackgroundColor(color)

        database =
            FirebaseDatabase.getInstance("https://todolist-app-e056a-default-rtdb.firebaseio.com")
                .getReference("todo")

        todoId = intent.getStringExtra(toodoId) ?: ""

        val taskRef = database.child(todoId)
        taskRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val todo = snapshot.getValue(TodoItem::class.java)
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

    private fun showAlertDialog(title : String? , detail : String?, status : String?, isComplete : String?,uidCompleted : String?) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_dialog, null)

        val todo = dialogView.findViewById<EditText>(R.id.title_input)
        val detailTodo = dialogView.findViewById<EditText>(R.id.task_input)
        val cancel = dialogView.findViewById<ImageView>(R.id.cancel)
        val save = dialogView.findViewById<Button>(R.id.btnSave)

        todo.setText(title)
        detailTodo.setText(detail)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_bg)
        dialog.show()
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        save.setOnClickListener {
            updateTodo(todo.text.toString(), detailTodo.text.toString(),status, isComplete, uidCompleted,dialog )
        }
    }

    private fun updateTodo(todo : String, detail : String,status : String?, isComplete : String?,uidCompleted : String?, finish : AlertDialog){
        database = Utils.firebaseDatabase.getReference(Utils.TODO)
        val taskId = database.push().key!!
        val uid = auth.currentUser?.uid
        val currentTime = Utils.getCurrentTimeWithFormat()
        val updateTask = InputTodo(taskId,uid!!,todo, detail, time = currentTime, status = status!!, isCompleted = isComplete!!, uid_completed = uidCompleted!!)

        val dbRef = database.child(todoId)
        dbRef.setValue(updateTask)
            .addOnSuccessListener {
                Toast.makeText(this, "Succes Update Todo", Toast.LENGTH_SHORT).show()
                finish.dismiss()
                finish()
            }
    }

    private fun deleteTodo() {
        val todoRef = database.child(todoId)

        todoRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Todo deleted successfully", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete todo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val toodoId = "TodoId"

    }
}