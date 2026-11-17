package com.huanco.centrotareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.huanco.centrotareas.model.CargaUi
import com.huanco.centrotareas.model.PrimosUi
import kotlin.math.roundToInt
import com.huanco.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val cargando = carga == CargaUi.Cargando
    val primos by vm.primos.collectAsState()
    val calculando = primos == PrimosUi.Calculando
    var limite by rememberSaveable { mutableStateOf("1000") }
    val hasta = limite.toIntOrNull()?.takeIf { it >= 2 }
    var toques by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = { toques++ }) {
                Text("Tócame: $toques")
            }
            TarjetaTarea("Tarea con progreso") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = vm::tareaLarga, enabled = !corriendo) {
                        Text("Iniciar")
                    }
                    OutlinedButton(onClick = vm::cancelar, enabled = corriendo) {
                        Text("Cancelar")
                    }
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${(progreso * 100).roundToInt()} %")
                Text(mensaje)
            }
            TarjetaTarea("Contar números primos") {
                OutlinedTextField(
                    value = limite,
                    onValueChange = { limite = it },
                    enabled = !calculando,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Hasta el número") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { hasta?.let(vm::contar) },
                        enabled = hasta != null && !calculando
                    ) { Text("Contar") }
                    OutlinedButton(onClick = vm::cancelarPrimos, enabled = calculando) {
                        Text("Cancelar")
                    }
                }
                when (val estado = primos) {
                    PrimosUi.Inicial -> Unit
                    PrimosUi.Calculando -> Text("Calculando...")
                    PrimosUi.Cancelado -> Text("Cancelado")
                    is PrimosUi.Listo -> Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Cantidad de primos: ${estado.cantidad}")
                            Text("Tiempo: ${estado.milisegundos} ms")
                        }
                    }
                }
            }
            TarjetaTarea("Dos cargas") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = simularError,
                        onCheckedChange = vm::cambiarSimularError,
                        enabled = !cargando
                    )
                    Text("Simular error")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = vm::cargarUnaPorUna, enabled = !cargando) {
                        Text("Una por una")
                    }
                    Button(onClick = vm::cargarALaVez, enabled = !cargando) {
                        Text("A la vez")
                    }
                }
                when (val estado = carga) {
                    CargaUi.Cargando -> CircularProgressIndicator()
                    is CargaUi.Exito -> Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFFE1F2E1),
                        contentColor = Color(0xFF205B30)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Modo: ${estado.modo}")
                            Text("Perfil: ${estado.perfil}")
                            Text("Pedidos: ${estado.pedidos}")
                            Text("Tiempo: ${estado.milisegundos} ms")
                        }
                    }
                    is CargaUi.Error -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Text(estado.mensaje, modifier = Modifier.padding(12.dp))
                        }
                        OutlinedButton(onClick = vm::cargarALaVez) {
                            Text("Reintentar")
                        }
                    }
                    CargaUi.Inicial -> Unit
                }
            }
        }
    }
}

@Composable
private fun TarjetaTarea(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}
