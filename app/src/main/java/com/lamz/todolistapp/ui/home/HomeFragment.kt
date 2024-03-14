package com.lamz.todolistapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.lamz.todolistapp.DetailActivity
import com.lamz.todolistapp.LoginActivity
import com.lamz.todolistapp.R
import com.lamz.todolistapp.TodoAdapter
import com.lamz.todolistapp.data.TodoItem
import com.lamz.todolistapp.data.TodoRepository
import com.lamz.todolistapp.databinding.FragmentHomeBinding
import com.lamz.todolistapp.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private val homeViewModel: HomeViewModel by viewModel()
    private lateinit var todoRepository: TodoRepository
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val colorTeks = ContextCompat.getColor(requireContext(), R.color.white)
        binding.text1.setTextColor(colorTeks)

        val user = auth.currentUser
        if (user != null) {
            binding.text1.text = getString(R.string.welcome_back_main, user.displayName)
        }

        val searchEditText = binding.search


        binding.homeMenu.setOnClickListener {
            showPopupMenu(it)
        }


        binding.rvTodo.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        todoRepository = TodoRepository()

        val loadingProgressBar = binding.loadingProgressBar

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }


        homeViewModel.fetchTodoList()

        homeViewModel.todoList.observe(viewLifecycleOwner) { todoList ->
            if (todoList.isNotEmpty()){
                updateUI(todoList)
                val emptyBanner = binding.emptyBanner.root
                emptyBanner.visibility = View.INVISIBLE
            }else{
                val emptyBanner = binding.emptyBanner.root
                emptyBanner.visibility = View.VISIBLE
            }


            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {

                    val query = s.toString().trim()


                    val filteredList = todoList.filter { todoItem ->
                        todoItem.title.contains(
                            query,
                            ignoreCase = true
                        ) || todoItem.detail.contains(query, ignoreCase = true)
                    }

                    updateUI(filteredList as ArrayList<TodoItem>)
                }
            })

        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.option_menu, popupMenu.menu)


        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu1 -> {
                    googleSignInClient.signOut()
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }


    private fun updateUI(todoList: ArrayList<TodoItem>) {

        val adapter = TodoAdapter(todoList)


        binding.rvTodo.adapter = adapter

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
                val todoRef =
                    Utils.firebaseDatabase.getReference("todo").child(clickedItem.todoId)
                todoRef.child(Utils.COMPLETED).setValue(if (isChecked) "yes" else "no")
                todoRef.child(Utils.UID_COMPLETED)
                    .setValue(if (isChecked) "${uidCompleted}_yes" else "")
                todoRef.child(Utils.STATUS).setValue(if (isChecked) "Complete" else "Incomplete")
            }
        })
    }


    companion object {
        const val TAG = "HomeFragment"
    }
}
