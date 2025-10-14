package com.mod5.ae4_abpro1_apilibros

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // URL de la API de Google Books
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    val api: BooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BooksApiService::class.java)
    }
}
