package com.mod5.ae4_abpro1_apilibros

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.RadioGroup
import android.widget.RadioButton
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    // ViewModel
    private lateinit var bookViewModel: BookViewModel

    // Variables de UI
    private lateinit var etQuery: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnSearch: ImageButton
    private lateinit var btnCancel: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var rgSearchType: RadioGroup
    private lateinit var rbTitle: RadioButton
    private lateinit var rbAuthor: RadioButton
    private lateinit var rvBooks: RecyclerView // Nuevo: para la lista de resultados

    // Adaptador para la lista de libros
    // NOTA: Se requiere crear BookAdapter.kt
    private lateinit var bookAdapter: BookAdapter // Usaremos un BookAdapter simple (definido aparte)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de la capa de MVVM
        val repository = BookRepository(RetrofitInstance.api)
        // Se utiliza una Factory simple para instanciar el ViewModel con el Repositorio
        val factory = BookViewModelFactory(repository)
        bookViewModel = ViewModelProvider(this, factory).get(BookViewModel::class.java)

        // Inicialización de la UI
        etQuery = findViewById(R.id.etQuery) // Usar R.id.etQuery en el nuevo layout
        tvStatus = findViewById(R.id.tvStatus) // Usar R.id.tvStatus
        btnSearch = findViewById(R.id.btnSearch)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
        rgSearchType = findViewById(R.id.rgSearchType)
        rbTitle = findViewById(R.id.rbTitle)
        rbAuthor = findViewById(R.id.rbAuthor)
        rvBooks = findViewById(R.id.rvBooks)


        // Configuración de RecyclerView
        bookAdapter = BookAdapter()
        rvBooks.layoutManager = LinearLayoutManager(this)
        rvBooks.adapter = bookAdapter

        // Configuración inicial de la UI
        tvStatus.text = "APIBooks por Peras con Manzanas. 🍐🍎"
        showLoading(false)

        // ------------------ OBSERVADORES (Manejo de UI con LiveData) ------------------

        // Observa el estado de carga del ViewModel
        bookViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        // Observa el mensaje de estado del ViewModel
        bookViewModel.statusMessage.observe(this) { message ->
            tvStatus.text = message
        }

        // Observa la lista de libros del ViewModel y actualiza el RecyclerView
        bookViewModel.books.observe(this) { booksList ->
            bookAdapter.submitList(booksList) // Actualiza el adaptador
            // Asegurarse de que la lista es visible (no se muestra en el layout provisto,
            // pero se asume que rvBooks está debajo de tvStatus en la pantalla)
            if (booksList.isNotEmpty()) {
                rvBooks.visibility = android.view.View.VISIBLE
            } else {
                rvBooks.visibility = android.view.View.GONE
            }
        }

        // ------------------ LISTENERS (Interacciones de Usuario) ------------------

        btnSearch.setOnClickListener {
            val query = etQuery.text.toString().trim()
            val searchType = when (rgSearchType.checkedRadioButtonId) {
                R.id.rbTitle -> "title"
                R.id.rbAuthor -> "author"
                else -> "general" // Default a búsqueda general si se añade
            }

            if (query.isNotEmpty()) {
                // Llama al método del ViewModel, que maneja la lógica de Coroutines
                bookViewModel.searchBooks(searchType, query)
            } else {
                tvStatus.text = "⚠️ Ingrese un criterio de búsqueda."
            }
        }

        // Cancela el Job manualmente (llama al método del ViewModel)
        btnCancel.setOnClickListener {
            bookViewModel.cancelCurrentSearch()
        }
    }

    // Ya no se requiere job.cancel() aquí, ya que el ViewModel se encarga automáticamente
    // a través de onCleared() y viewModelScope.
    override fun onDestroy() {
        super.onDestroy()
        // La cancelación en onDestroy() es manejada por viewModelScope.onCleared()
    }

    // Función para mostrar/ocultar carga (se mantiene)
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        btnSearch.isEnabled = !isLoading
        btnCancel.isEnabled = isLoading
        if (isLoading) {
            rvBooks.visibility = android.view.View.GONE // Oculta la lista mientras carga
        }
    }
}


// --------------------------------------------------------------------------
// NOTA: Se requiere una Factory para instanciar el ViewModel con dependencias.
// --------------------------------------------------------------------------



class BookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --------------------------------------------------------------------------
// NOTA: Se requiere un BookAdapter simple para RecyclerView.
// (Se omite la implementación detallada para no extender demasiado el código,
// pero debe heredar de ListAdapter o RecyclerView.Adapter).
// --------------------------------------------------------------------------

// Ejemplo de BookAdapter.kt (debe ser implementado)
/*
class BookAdapter : ListAdapter<BookDisplay, BookAdapter.BookViewHolder>(BookDiffCallback()) {
    // ... implementación del ViewHolder, onCreateViewHolder, onBindViewHolder, DiffUtil.ItemCallback
*/