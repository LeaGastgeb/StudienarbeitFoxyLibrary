package com.example.studienarbeitfoxylibrary.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studienarbeitfoxylibrary.R
import com.example.studienarbeitfoxylibrary.repository.database.Book

class BookListAdapter(var content:ArrayList<Book>):RecyclerView.Adapter<BookListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return content.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = content[position]
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        holder.tvPage.text = book.pageCount.toString()
        holder.tvCount.text = book.publicationDate
    }
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        val tvTitle:TextView = itemView.findViewById(R.id.book_title)
        val tvAuthor:TextView = itemView.findViewById(R.id.book_author)
        val tvPage:TextView = itemView.findViewById(R.id.book_page)
        val tvCount:TextView = itemView.findViewById(R.id.book_date)
    }
    fun updateContent(content: ArrayList<Book>)
    {
        this.content = content
        notifyDataSetChanged()
    }
}