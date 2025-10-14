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

    // Variables de la interfaz
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
    private lateinit var bookAdapter: BookAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //MVVM
        val repository = BookRepository(RetrofitInstance.api)

        // con el Factory simple se instancia el ViewModel con el Repositorio
        val factory    = BookViewModelFactory(repository)
        bookViewModel   = ViewModelProvider(this, factory).get(BookViewModel::class.java)

        // Inicialización elementos de la interfaz
        etQuery      = findViewById(R.id.etQuery)
        tvStatus     = findViewById(R.id.tvStatus)
        btnSearch    = findViewById(R.id.btnSearch)
        btnCancel    = findViewById(R.id.btnCancel)
        progressBar  = findViewById(R.id.progressBar)
        rgSearchType = findViewById(R.id.rgSearchType)
        rbTitle      = findViewById(R.id.rbTitle)
        rbAuthor     = findViewById(R.id.rbAuthor)
        rvBooks      = findViewById(R.id.rvBooks)

        // Configuración de RecyclerView
        bookAdapter           = BookAdapter()
        rvBooks.layoutManager = LinearLayoutManager(this)
        rvBooks.adapter       = bookAdapter

        // Configuración inicial de la interfaz
        tvStatus.text = "APIBooks por Peras con Manzanas 🍐🍎"
        showLoading(false)

        // Implementación de los Observer para los cambios de la interfaz con LiveData)
        // ***************************************************************************
        // Observe para el estado de carga del ViewModel
        bookViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        // Observe para el mensaje de estado del ViewModel
        bookViewModel.statusMessage.observe(this) { message ->
            tvStatus.text = message
        }

        // Observe para la lista de libros del ViewModel y actualiza el RecyclerView
        bookViewModel.books.observe(this) { booksList ->
            bookAdapter.submitList(booksList) // Actualiza el adaptador
            if (booksList.isNotEmpty()) {
                rvBooks.visibility = android.view.View.VISIBLE
            } else {
                rvBooks.visibility = android.view.View.GONE
            }
        }

        // Implementación de los Listeners para las interacciones con el usuario
        // *********************************************************************

        btnSearch.setOnClickListener {
            val query = etQuery.text.toString().trim()
            val searchType = when (rgSearchType.checkedRadioButtonId) {
                R.id.rbTitle  -> "title"
                R.id.rbAuthor -> "author"
                else -> "general" // Por default 'búsqueda general'
            }

            if (query.isNotEmpty()) {
                // Llama al ViewModel que maneja la lógica de Coroutines
                bookViewModel.searchBooks(searchType, query)
            } else {
                tvStatus.text = "⚠️ Ingrese una cadena para la búsqueda."
            }
        }

        // Cancela el Job manualmente
        btnCancel.setOnClickListener {
            bookViewModel.cancelCurrentSearch()
        }
    }

    override fun onDestroy() {
        super.onDestroy() // Es manejada por viewModelScope.onCleared()

    }

    // Función para mostrar/ocultar carga (heredada de ApiClima)
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        btnSearch.isEnabled    = !isLoading
        btnCancel.isEnabled    = isLoading
        if (isLoading) {
            rvBooks.visibility = android.view.View.GONE // Oculta la lista mientras carga
        }
    }
}

class BookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}