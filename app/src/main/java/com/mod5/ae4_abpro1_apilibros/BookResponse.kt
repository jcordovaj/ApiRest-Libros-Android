package com.mod5.ae4_abpro1_apilibros

data class BooksResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val publishedDate: String?,
    val description: String?
)

/**
 * Data Class para mostrar en la lista final, añadiendo la lógica de negocio simulada.
 */
data class BookDisplay(
    val id: String,
    val title: String,
    val authors: String,
    val publishedDate: String?,
    val description: String?,
    val isAvailable: Boolean,
    val stock: Int
)