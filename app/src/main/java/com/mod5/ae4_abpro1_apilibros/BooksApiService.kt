package com.mod5.ae4_abpro1_apilibros

import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApiService {

    /**
     * Este 'endpoint' permite buscar libros en la API de Google Books.
     * @param query Es la cadena de búsqueda, ej: "El Principito" o "Pepo".
     * @param maxResults Limita el número de resultados.
     */
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10 // Mostrará 10 resultados para el MVP
    ): BooksResponse
}