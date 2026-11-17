# Registro de Prompt y Decisiones (Fase 2)

## Prompt Original
> Actúa como un desarrollador Android con experiencia en Kotlin, Jetpack Compose, corrutinas y arquitectura MVVM + Repository. Estoy aprendiendo, así que implementa los cambios de forma clara y explícame brevemente las decisiones importantes.
> 
> Trabaja sobre el proyecto CentroTareas y revisa primero los archivos existentes, especialmente `TareasRepository`, `TareasRepositoryLocal`, `TareasViewModel`, `PantallaTareas` y los estados de UI. Conserva la arquitectura y el comportamiento actual de la aplicación.
> 
> Implementa estas tres mejoras de la Fase 2:
> 1. **Progreso desde el Repository**: Mueve la tarea simulada con progreso al Repository. Expón el avance como `Flow<Float>`, emitiendo valores entre 0 y 1. Usa `flow { ... }` y `flowOn(Dispatchers.Default)`. El ViewModel debe recolectar el flujo y publicar el progreso y el estado de la tarea para la pantalla. La tarea debe seguir pudiendo cancelarse.
> 2. **Reintento automático**: Reintenta las cargas automáticamente hasta un máximo de tres intentos. Aumenta la espera entre intentos, por ejemplo, 500 ms y luego 1000 ms. Si se agotan los intentos, conserva el mensaje de error y la opción actual de reintentar manualmente. Si ocurre una `CancellationException`, vuelve a lanzarla de inmediato; no la conviertas en un error ni reintentes la operación.
> 3. **Comparación acumulada de tiempos**: Guarda en el ViewModel los tiempos de las cargas exitosas «Una por una» y «A la vez». Muéstralos en una tabla sencilla en la pantalla, indicando el modo y el tiempo en milisegundos. Conserva los resultados anteriores mientras la app siga abierta.
> 
> Reglas:
> - Mantén la lógica de negocio en el Repository o el ViewModel; la pantalla solo presenta el estado y envía eventos.
> - No uses `GlobalScope`, `runBlocking` ni `Thread.sleep`.
> - No agregues dependencias si no son necesarias.
> - No borres ni reemplazes las funciones existentes sin comprobar sus usos.
> - Mantén la opción «Simular error» y asegúrate de que la cancelación siga funcionando.
> - Trabaja únicamente en esta rama `mejora-ia`; no hagas commits ni push.
> - Al terminar, resume los archivos modificados y cómo probar cada mejora. Actualiza `PROMPTS.md` con este prompt, un resumen de tu respuesta, los cambios realizados y el motivo de las decisiones.

---

## Resumen de la Respuesta

Se implementaron las tres mejoras solicitadas para la Fase 2 del proyecto **CentroTareas**, manteniendo estrictamente la arquitectura MVVM + Repository, el uso correcto de Corrientes y Flow, y la reactividad con Jetpack Compose.

---

## Cambios Realizados

1. **`TareasRepository.kt` y `TareasRepositoryLocal.kt`**:
   - Se añadió la función `simularProgreso(): Flow<Float>` a la interfaz `TareasRepository`.
   - En `TareasRepositoryLocal`, se implementó `simularProgreso()` utilizando el constructor de flujos `flow { ... }`, emitiendo valores de progreso de `0f` a `1f` cada `40ms`, y aplicando `flowOn(Dispatchers.Default)` para ejecutar el trabajo pesado fuera del hilo principal.

2. **`TareasViewModel.kt`**:
   - Se actualizó `tareaLarga()` para consumir `repo.simularProgreso()` colectormente dentro de `viewModelScope`, actualizando `_progreso` y manejando la cancelación correctamente.
   - Se añadió una lista observable de historial de cargas exitosas (`_historialCargas` / `historialCargas`) para conservar y acumular los resultados de las cargas mientras la app siga abierta.
   - Se implementó la función auxiliar `retryWithBackoff` para reintentar automáticamente las operaciones de red/carga hasta 3 intentos con retardos incrementales de 500 ms y 1000 ms, propagando inmediatamente cualquier `CancellationException`.

3. **`PantallaTareas.kt`**:
   - Se recolectó el estado `historialCargas` desde el ViewModel.
   - Se incorporó una tabla de comparación de tiempos acumulados en la tarjeta "Dos cargas", mostrando de manera clara el modo ("Una por una" o "A la vez") y el tiempo en milisegundos de cada ejecución exitosa previa.

---

## Motivo de las Decisiones Arquitectónicas

- **Flujos en el Repositorio (`flow` y `flowOn`)**: Mover la lógica de emisión de progreso al repositorio encapsula los datos y el cálculo, respetando el principio de que los repositorios proveen fuentes de datos reactivas (`Flow`). El uso de `flowOn(Dispatchers.Default)` garantiza que las demoras y cálculos del bucle ocurran en el hilo de trabajo de CPU.
- **Manejo de Reintentos con Backoff Exponencial / Lineal**: Centralizar los reintentos mediante un bloque suspensivo configurable evita duplicación de código en las funciones de carga y respeta las restricciones de manejo de excepciones (`CancellationException` se relanza sin intentar reintentar).
- **Acumulación de Historial en el ViewModel**: Almacenar una lista inmutable de ejecuciones exitosas en un `StateFlow` dentro del ViewModel permite mantener la información persistente en memoria mientras el ciclo de vida del ViewModel esté activo (durante toda la sesión de la app), separando la persistencia temporal de estado de la UI (la cual solo renderiza lo que recibe).
