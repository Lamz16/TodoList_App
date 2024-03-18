package com.lamz.todolistapp

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lamz.todolistapp.data.model.MainViewModel
import com.lamz.todolistapp.databinding.ActivityMainBinding
import com.lamz.todolistapp.databinding.AlertDialogBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var alertDialogBinding: AlertDialogBinding
    private val mainViewModel: MainViewModel by viewModel()
    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertDialogBinding = AlertDialogBinding.inflate(layoutInflater)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
        }

        if (isDialogShown) {
            showAlertDialog()
        }


        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setBackgroundColor(Color.TRANSPARENT)


        val indexes = listOf(1, 2)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogShown", isDialogShown)
        val todoInput = alertDialogBinding.titleInput.text.toString()
        val detailInput = alertDialogBinding.taskInput.text.toString()
        outState.putString("todoInput", todoInput)
        outState.putString("detailInput", detailInput)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val saveTitle = savedInstanceState.getString("todoInput")
        val saveDetail = savedInstanceState.getString("detailInput")
        alertDialogBinding.titleInput.setText(saveTitle)
        alertDialogBinding.taskInput.setText(saveDetail)
    }

    private fun showAlertDialog() {
        isDialogShown = true
        val builder = AlertDialog.Builder(this)
        val view = alertDialogBinding.root
        alertDialogBinding.apply {
            val todo = titleInput
            val detail = taskInput


            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_bg)
            dialog.show()

            cancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val currentTodoTitle = todo.text.toString()
                val currentTodoDetail = detail.text.toString()
                createTodo(currentTodoTitle, currentTodoDetail, dialog)
            }
        }

    }

    private fun createTodo(todo: String, detail: String, dialog: AlertDialog) {
        mainViewModel.createTodo(todo, detail)
        mainViewModel.createTodoSuccess.observe(this) { success ->
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