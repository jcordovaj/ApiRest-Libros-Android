package com.mod5.ae4_abpro1_apilibros

import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApiService {

    /**
     * Endpoint para buscar libros en la API de Google Books.
     * @param query La cadena de búsqueda, ej: "intitle:El Principito" o "inauthor:Antoine de Saint-Exupéry".
     * @param maxResults Limita el número de resultados.
     */
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10 // Limitar a 10 resultados para la demo
    ): BooksResponse
}