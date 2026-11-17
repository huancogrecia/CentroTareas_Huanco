package com.huanco.centrotareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huanco.centrotareas.data.TareasRepositoryLocal
import com.huanco.centrotareas.ui.PantallaTareas
import com.huanco.centrotareas.ui.theme.CentroTareasTheme
import com.huanco.centrotareas.viewmodel.TareasViewModel
import com.huanco.centrotareas.viewmodel.tareasViewModelFactory

class MainActivity : ComponentActivity() {
    private val factory = tareasViewModelFactory(TareasRepositoryLocal())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CentroTareasTheme {
                val vm: TareasViewModel = viewModel(factory = factory)
                PantallaTareas(vm)
            }
        }
    }
}
