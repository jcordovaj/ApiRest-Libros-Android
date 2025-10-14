package com.mod5.ae4_abpro1_apilibros

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter para mostrar la lista de BookDisplay en RecyclerView.
 * Usa ListAdapter con DiffUtil para actualizaciones eficientes.
 */
class BookAdapter : ListAdapter<BookDisplay, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Infla el layout de un ítem de libro (requiere item_book.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book)
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs de TextViews en item_book.xml (a definir)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        private val tvAvailability: TextView = itemView.findViewById(R.id.tvBookAvailability)

        fun bind(book: BookDisplay) {
            tvTitle.text = book.title
            tvAuthor.text = "Autor(es): ${book.authors}"

            val availabilityText = if (book.isAvailable) {
                "✅ DISPONIBLE (${book.stock} unid.)"
            } else {
                "❌ AGOTADO"
            }
            tvAvailability.text = availabilityText
        }
    }
}

/**
 * Callback para calcular las diferencias entre listas (optimización de RecyclerView).
 */
class BookDiffCallback : DiffUtil.ItemCallback<BookDisplay>() {
    override fun areItemsTheSame(oldItem: BookDisplay, newItem: BookDisplay): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BookDisplay, newItem: BookDisplay): Boolean {
        return oldItem == newItem
    }
}