package com.lamz.todolistapp.ui.completed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.lamz.todolistapp.DetailActivity
import com.lamz.todolistapp.R
import com.lamz.todolistapp.TodoAdapter
import com.lamz.todolistapp.data.TodoItem
import com.lamz.todolistapp.databinding.FragmentCompletedBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CompletedFragment : Fragment() {

    private lateinit var binding: FragmentCompletedBinding
    private lateinit var auth: FirebaseAuth
    private val completedViewModel : CompletedViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val colorTeks = ContextCompat.getColor(requireContext(), R.color.white)
        binding.text1.setTextColor(colorTeks)

        completedViewModel.fetchTodoList()
        completedViewModel.todoList.observe(viewLifecycleOwner){
            updateUI(it)
        }

        val loadingProgressBar = binding.loadingProgressBar

        completedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Atur visibilitas ProgressBar berdasarkan status loading
            loadingProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.rvTodo.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun updateUI(todoList: ArrayList<TodoItem>) {
        // Membuat atau memperbarui adapter RecyclerView
        val adapter = TodoAdapter(todoList)

        // Mengatur adapter ke RecyclerView
        binding.rvTodo.adapter = adapter

        // Menambahkan listener untuk meng-handle item yang diklik
        adapter.setOnItemClickListener(object : TodoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val clickedItem = todoList[position]
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra(DetailActivity.toodoId, clickedItem.todoId)
                startActivity(intent)
            }

            override fun onCheckboxChanged(position: Int, isChecked: Boolean) {
                val uidCompleted = FirebaseAuth.getInstance().currentUser?.uid
                val clickedItem = todoList[position]
                val todoRef = FirebaseDatabase.getInstance("https://todolist-app-e056a-default-rtdb.firebaseio.com").getReference("todo").child(clickedItem.todoId)
                todoRef.child("completed").setValue(if (isChecked) "yes" else "no")
                todoRef.child("uid_completed").setValue(if (isChecked) "${uidCompleted}_yes" else "")
                todoRef.child("status").setValue(if (isChecked) "Complete" else "Incomplete")
            }
        })
    }

    companion object {
        const val TAG = "CompletedFragment"
    }
}
