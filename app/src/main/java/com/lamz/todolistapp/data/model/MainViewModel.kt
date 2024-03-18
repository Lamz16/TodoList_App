package com.lamz.todolistapp.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamz.todolistapp.data.TodoRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: TodoRepository): ViewModel() {
    private val _createTodoSuccess = MutableLiveData<Boolean>()
    val createTodoSuccess: LiveData<Boolean> get() = _createTodoSuccess

    fun createTodo(todo: String, detail: String) {
        viewModelScope.launch{
            try {
                repository.createTodo(todo, detail)
                _createTodoSuccess.value = true
            } catch (e: Exception) {
                _createTodoSuccess.value = false
            }
        }
    }
}