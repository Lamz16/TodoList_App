package com.lamz.todolistapp

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lamz.todolistapp.data.model.MainViewModel
import com.lamz.todolistapp.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setBackgroundColor(Color.TRANSPARENT)


        val indexes = listOf(1,2)
        indexes.forEach { index ->
            navView.menu.getItem(index).isEnabled = false
        }
        val color = ContextCompat.getColor(this, R.color.color_2)
        binding.container.setBackgroundColor(color)

        navView.setupWithNavController(navController)
        binding.fab.setOnClickListener {
            showAlertDialog()
        }

    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_dialog, null)


        val todo = dialogView.findViewById<EditText>(R.id.title_input)
        val detail = dialogView.findViewById<EditText>(R.id.task_input)
        val cancel = dialogView.findViewById<ImageView>(R.id.cancel)
        val save = dialogView.findViewById<Button>(R.id.btnSave)


        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_bg)
        dialog.show()
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        save.setOnClickListener {
            createTodo(todo.text.toString(), detail.text.toString(), dialog)
        }
    }

    private fun createTodo(todo : String, detail : String, dialog : AlertDialog){
        mainViewModel.createTodo(todo,detail)
        mainViewModel.createTodoSuccess.observe(this){ success ->
            if (success) {
                Toast.makeText(this, "Success Create Task", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Failed to create task", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }


}