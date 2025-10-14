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

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    // LiveData para observar el estado de la UI en la Activity
    val books: MutableLiveData<List<BookDisplay>> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean>       = MutableLiveData(false)
    val statusMessage: MutableLiveData<String>    = MutableLiveData("Ingrese un título o autor.")

    private var currentJob: Job? = null // Para el control de cancelación

    /**
     * Inicia la búsqueda de libros, usando Coroutines.
     * Mantiene la misma estructura de concurrencia (async/await) de la versión con ApiClima.
     * @param searchType Criterio de búsqueda (título o autor).
     * @param query La palabra clave a buscar.
     */
    fun searchBooks(searchType: String, query: String) {
        // Cancela Job activo antes de iniciar uno nuevo
        currentJob?.cancel()

        // Formato de query para Google Books API: intitle: o inauthor:
        val apiQuery = when(searchType) {
            "title" -> "intitle:$query"
            "author" -> "inauthor:$query"
            else -> query // Por defecto
        }

        // 'launch' en el viewModelScope. El Job se cancela automáticamente cuando el ViewModel se limpia (onCleared()).
        currentJob = viewModelScope.launch {
            statusMessage.value = "Consultando libros por $searchType: \"$query\"..."
            isLoading.value = true

            try {
                // Tarea en 2do. plano. Llama a la función suspendida del Repositorio, que se ejecuta en el hilo IO
                val result = withContext(Dispatchers.IO) {

                    // Simula proceso paralelo (manteniendo la lógica del caso ApiClima)
                    val apiCall = async {
                        repository.searchBooksAndProcess(apiQuery) // Esta es la tarea asíncrona principal
                    }

                    // Tarea fake paralela
                    val parallelTask = async {
                        delay(3000)
                        "Simulación de tarea de carga paralela terminada."
                    }

                    Log.d("Coroutines", parallelTask.await())
                    apiCall.await() // Acá se espera el resultado de la llamada
                }

                // El código, después de withContext, se reanuda en el hilo principal (Main)
                if (result.isNotEmpty()) {
                    books.value         = result // Aquí actualiza LiveData
                    statusMessage.value = "✅ Se encontraron ${result.size} libros para \"$query\"."
                } else {
                    books.value = emptyList()
                    statusMessage.value = "❌ No se encontraron libros para \"$query\". Intente refinar la búsqueda."
                }

            } catch (e: CancellationException) {
                // Se captura el log si el Job se cancela, por ejemplo,  por una nueva búsqueda o si el usuario cancela (manualmente)
                Log.e("APIBooks", "Solicitud cancelada.")
                statusMessage.value = "Búsqueda cancelada 🚫"
            } catch (e: Exception) {
                // Otros errores, ejemplo, red, parseo, etc.
                Log.e("APIBooks", "Error al intentar obtener libros: ${e.message}")
                statusMessage.value = "❌ Error: La consulta falló. ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Función que cancela manualmente el Job (si el usuario presiona 'X', antes de los 3 seg.).
    fun cancelCurrentSearch() {
        if (currentJob?.isActive == true) {
            currentJob?.cancel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}