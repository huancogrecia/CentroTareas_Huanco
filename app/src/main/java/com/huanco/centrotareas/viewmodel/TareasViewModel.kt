package com.huanco.centrotareas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanco.centrotareas.data.TareasRepository
import com.huanco.centrotareas.model.CargaUi
import com.huanco.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {
    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()

    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    private val _historialCargas = MutableStateFlow<List<CargaUi.Exito>>(emptyList())
    val historialCargas: StateFlow<List<CargaUi.Exito>> = _historialCargas.asStateFlow()

    private var trabajo: Job? = null
    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    fun tareaLarga() {
        if (_corriendo.value) return

        trabajo = viewModelScope.launch {
            _corriendo.value = true
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                repo.simularProgreso().collect { avance ->
                    _progreso.value = avance
                }
                _mensaje.value = "Terminó"
            } catch (cancelacion: CancellationException) {
                _mensaje.value = "Cancelado"
                throw cancelacion
            } finally {
                _corriendo.value = false
            }
        }
    }

    fun cancelar() {
        trabajo?.cancel()
    }

    private var trabajoPrimos: Job? = null

    fun contar(hasta: Int) {
        if (hasta < 2 || _primos.value == PrimosUi.Calculando) return

        trabajoPrimos = viewModelScope.launch {
            _primos.value = PrimosUi.Calculando
            val inicio = System.currentTimeMillis()
            try {
                val cantidad = repo.contarPrimos(hasta)
                _primos.value = PrimosUi.Listo(cantidad, System.currentTimeMillis() - inicio)
            } catch (cancelacion: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw cancelacion
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    fun cargarUnaPorUna() = cargar("Una por una") { simularError ->
        val perfil = repo.cargarPerfil()
        val pedidos = repo.cargarPedidos(simularError)
        perfil to pedidos.size
    }

    fun cargarALaVez() = cargar("A la vez") { simularError ->
        coroutineScope {
            val perfil = async { repo.cargarPerfil() }
            val pedidos = async { repo.cargarPedidos(simularError) }
            perfil.await() to pedidos.await().size
        }
    }

    private suspend fun <T> retryWithBackoff(
        times: Int = 3,
        initialDelay: Long = 500,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt == times - 1) {
                    throw e
                }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        throw IllegalStateException("Should not reach here")
    }

    private fun cargar(modo: String, obtenerDatos: suspend (Boolean) -> Pair<String, Int>) {
        if (_carga.value == CargaUi.Cargando) return

        viewModelScope.launch {
            _carga.value = CargaUi.Cargando
            val inicio = System.nanoTime()
            try {
                val (perfil, pedidos) = retryWithBackoff(times = 3, initialDelay = 500) {
                    obtenerDatos(_simularError.value)
                }
                val milisegundos = (System.nanoTime() - inicio) / 1_000_000
                val exito = CargaUi.Exito(modo, perfil, pedidos, milisegundos)
                _carga.value = exito
                _historialCargas.value = _historialCargas.value + exito
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                val detalle = error.message ?: "Error desconocido"
                _carga.value = CargaUi.Error("No se pudo cargar: $detalle")
            }
        }
    }
}
