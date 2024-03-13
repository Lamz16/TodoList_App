package com.lamz.todolistapp.di

import com.lamz.todolistapp.data.TodoRepository
import com.lamz.todolistapp.data.model.MainViewModel
import com.lamz.todolistapp.ui.completed.CompletedViewModel
import com.lamz.todolistapp.ui.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { TodoRepository() }
    viewModel { HomeViewModel(get()) }
    viewModel { CompletedViewModel(get()) }
    viewModel { MainViewModel(get()) }
}