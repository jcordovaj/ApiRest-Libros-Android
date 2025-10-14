package com.mod5.ae4_abpro1_apilibros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import android.util.Log
// import com.example.apibooks.BookRepository
// import com.example.apibooks.BookDisplay

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    // LiveData para observar el estado de la UI en la Activity
    val books: MutableLiveData<List<BookDisplay>> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val statusMessage: MutableLiveData<String> = MutableLiveData("Biblioteca Pelotillehue 🏛️: Ingrese un título o autor.")

    private var currentJob: Job? = null // Para el control de cancelación

    /**
     * Inicia la búsqueda de libros, usando Coroutines.
     * Mantiene la misma estructura de concurrencia (async/await) para la demostración.
     * @param searchType Criterio de búsqueda (título o autor).
     * @param query La palabra clave a buscar.
     */
    fun searchBooks(searchType: String, query: String) {
        // Cancelar cualquier Job activo antes de iniciar uno nuevo
        currentJob?.cancel()

        // Formato de query para Google Books API: intitle: o inauthor:
        val apiQuery = when(searchType) {
            "title" -> "intitle:$query"
            "author" -> "inauthor:$query"
            else -> query // Por defecto, búsqueda general
        }

        // 'launch' en el viewModelScope. El Job se cancela automáticamente cuando el ViewModel se limpia (onCleared()).
        currentJob = viewModelScope.launch {
            statusMessage.value = "Consultando libros por $searchType: \"$query\"..."
            isLoading.value = true

            try {
                // Tarea en 2do. plano. Llama a la función suspendida del Repositorio, que se ejecuta en el hilo IO
                val result = withContext(Dispatchers.IO) {

                    // Emulación de proceso paralelo (manteniendo la lógica del ejemplo base)
                    val apiCall = async {
                        repository.searchBooksAndProcess(apiQuery) // Tarea asíncrona principal
                    }

                    // Tarea fake paralela
                    val parallelTask = async {
                        delay(2000)
                        "Tarea fake paralela de simulación de carga terminada."
                    }

                    Log.d("Coroutines", parallelTask.await())
                    apiCall.await() // Acá espera el resultado de la llamada al Repositorio
                }

                // El código después de withContext se reanuda en el hilo principal (Main)
                if (result.isNotEmpty()) {
                    books.value = result // Actualiza LiveData
                    statusMessage.value = "✅ Se encontraron ${result.size} libros para \"$query\"."
                } else {
                    books.value = emptyList()
                    statusMessage.value = "❌ No se encontraron libros para \"$query\". Intente refinar la búsqueda."
                }

            } catch (e: CancellationException) {
                // Se captura si el Job se cancela (ej: por una nueva búsqueda o si el usuario cancela)
                Log.e("APIBooks", "La solicitud fue cancelada.")
                statusMessage.value = "Búsqueda cancelada 🚫"
            } catch (e: Exception) {
                // Errores de red, parseo, etc.
                Log.e("APIBooks", "Error al obtener libros: ${e.message}")
                statusMessage.value = "❌ Error: La consulta falló. ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Función para cancelar manualmente el Job (si el usuario presiona Cancelar).
     */
    fun cancelCurrentSearch() {
        if (currentJob?.isActive == true) {
            currentJob?.cancel()
        }
    }

    /**
     * Mantiene la capacidad de la aplicación de base de crear un nuevo Job si se canceló.
     * Aunque en ViewModelScope no es estrictamente necesario ya que la cancelación
     * del job actual antes de lanzar uno nuevo ya maneja el estado.
     */
    // No necesitamos re-crear el Job. viewModelScope.launch() crea uno nuevo si el anterior terminó/canceló.
    // Simplemente nos aseguramos de cancelar el previo con cancelCurrentSearch().

    // El Job se cancela automáticamente en onCleared() del ViewModel,
    // que se llama cuando la Activity/Fragment se destruye permanentemente.
    // Esto reemplaza el job.cancel() en onDestroy() o onStop() de la Activity.
    override fun onCleared() {
        super.onCleared()
        // No es necesario llamar explícitamente a currentJob?.cancel() aquí, ya que viewModelScope se encarga,
        // pero lo mantenemos para ser explícitos si fuera un scope custom.
        currentJob?.cancel()
    }
}