package com.mod5.ae4_abpro1_apilibros

import kotlin.random.Random

class BookRepository(private val apiService: BooksApiService) {

    /**
     * Llama a la API y procesa la respuesta para añadir la lógica de negocio (disponibilidad).
     * @param query La cadena de búsqueda.
     */
    suspend fun searchBooksAndProcess(query: String): List<BookDisplay> {
        val response = apiService.searchBooks(query)

        // Mapea la respuesta de la API a nuestro modelo de display con la lógica de negocio
        return response.items?.map { bookItem ->
            val isAvailable = Random.nextBoolean() // Simula si hay disponibilidad o no (50% de probabilidad)
            val stock = if (isAvailable) Random.nextInt(1, 5) else 0 // Si hay disponibilidad, simula: 1-4 libros

            BookDisplay(
                id = bookItem.id,
                title = bookItem.volumeInfo.title ?: "Título Desconocido",
                authors = bookItem.volumeInfo.authors?.joinToString(", ") ?: "Autor Desconocido",
                publishedDate = bookItem.volumeInfo.publishedDate,
                description = bookItem.volumeInfo.description,
                isAvailable = isAvailable,
                stock = stock
            )
        } ?: emptyList()
    }
}