package com.huanco.centrotareas.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.huanco.centrotareas.data.TareasRepository

fun tareasViewModelFactory(repo: TareasRepository): ViewModelProvider.Factory =
    viewModelFactory {
        initializer { TareasViewModel(repo) }
    }
