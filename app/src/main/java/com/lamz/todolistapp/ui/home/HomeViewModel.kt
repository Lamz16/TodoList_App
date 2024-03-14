package com.lamz.todolistapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamz.todolistapp.data.TodoItem
import com.lamz.todolistapp.data.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _todoList = MutableLiveData<ArrayList<TodoItem>>()
    val todoList: LiveData<ArrayList<TodoItem>> get() = _todoList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        _isLoading.value = false
    }

    fun fetchTodoList() {

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTodoList { todoList ->
                _todoList.postValue(todoList)
                _isLoading.postValue(false)
            }
        }
    }
}

