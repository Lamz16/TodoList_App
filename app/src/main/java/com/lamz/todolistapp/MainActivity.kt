package com.lamz.todolistapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.lamz.todolistapp.data.model.MainViewModel
import com.lamz.todolistapp.databinding.ActivityMainBinding
import com.lamz.todolistapp.databinding.AlertDialogBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var _alertDialogBinding: AlertDialogBinding? = null
    private val alertDialogBinding get() = _alertDialogBinding
    private val mainViewModel: MainViewModel by viewModel()
    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            isDialogShown = savedInstanceState.getBoolean("isDialogShown", false)
        }
        if (isDialogShown) showAlertDialog()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.btnNotes.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_home) {
                navController.navigate(R.id.navigation_home)
            }
        }
        binding.btnArchive.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_completed) {
                navController.navigate(R.id.navigation_completed)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val notesSelected = destination.id == R.id.navigation_home
            styleSwitcher(notesSelected)
        }

        binding.container.setBackgroundColor(ContextCompat.getColor(this, R.color.surface_nature))
        binding.fab.setOnClickListener { showAlertDialog() }
    }

    private fun styleSwitcher(notesSelected: Boolean) {
        val selected = ContextCompat.getColor(this, R.color.forest)
        val unselected = ContextCompat.getColor(this, android.R.color.transparent)
        val selectedText = ContextCompat.getColor(this, R.color.white)
        val unselectedText = ContextCompat.getColor(this, R.color.text_secondary)

        binding.btnNotes.backgroundTintList = ColorStateList.valueOf(if (notesSelected) selected else unselected)
        binding.btnArchive.backgroundTintList = ColorStateList.valueOf(if (notesSelected) unselected else selected)
        binding.btnNotes.setTextColor(if (notesSelected) selectedText else unselectedText)
        binding.btnArchive.setTextColor(if (notesSelected) unselectedText else selectedText)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogShown", isDialogShown)
        outState.putString("todoInput", alertDialogBinding?.titleInput?.text.toString())
        outState.putString("detailInput", alertDialogBinding?.taskInput?.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        alertDialogBinding?.titleInput?.setText(savedInstanceState.getString("todoInput"))
        alertDialogBinding?.taskInput?.setText(savedInstanceState.getString("detailInput"))
    }

    private fun showAlertDialog() {
        isDialogShown = true
        val builder = AlertDialog.Builder(this)
        _alertDialogBinding = AlertDialogBinding.inflate(layoutInflater)
        val view = alertDialogBinding?.root

        alertDialogBinding?.apply {
            val todo = titleInput
            val detail = taskInput
            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_bg)
            dialog.show()

            cancel.setOnClickListener {
                dialog.dismiss()
                isDialogShown = false
            }
            btnSave.setOnClickListener {
                createTodo(todo.text.toString(), detail.text.toString(), dialog)
            }
        }
    }

    private fun createTodo(todo: String, detail: String, dialog: AlertDialog) {
        mainViewModel.createTodo(todo, detail)
        mainViewModel.createTodoSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                isDialogShown = false
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                isDialogShown = false
            }
        }
    }
}