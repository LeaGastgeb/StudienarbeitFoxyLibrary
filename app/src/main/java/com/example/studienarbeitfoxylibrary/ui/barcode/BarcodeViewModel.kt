package com.example.studienarbeitfoxylibrary.ui.barcode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studienarbeitfoxylibrary.repository.database.Book
import com.example.studienarbeitfoxylibrary.repository.repository.AppRepository
import kotlinx.coroutines.launch

class BarcodeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private var LiveBookList = repository.getLiveDataBooks()

    suspend fun insert(isbn: String, title: String, author: String, publisher: String, publicationDate: String, pageCount: String, price: String, genre: String, language: String, signature: String, borrowed: String, borrowedTo: String, borrowedOn: String, borrowedUntil: String, rating: String, comment: String, cover: String) {
        viewModelScope.launch {
            val book = Book(0, isbn, title, author, publisher, publicationDate, pageCount, price, genre, language, signature, borrowed, borrowedTo, borrowedOn, borrowedUntil, rating, comment, cover)
            repository.insert(book)
        }
    }

    suspend fun update(book: Book) {
        viewModelScope.launch {
            repository.update(book)
        }

    }

    suspend fun delete(book: Book) {
        viewModelScope.launch {
            repository.delete(book)
        }
    }

    suspend fun getBookById(bookId: Long): Book? {
        var book: Book? = null
        viewModelScope.launch {
            book = repository.getBookById(bookId)
        }
        return book
    }

    suspend fun getBookByIsbn(isbn: String): Book? {
        var book: Book? = null
        viewModelScope.launch {
            book = repository.getBookByIsbn(isbn)
        }
        return book
    }

    suspend fun getBookByTitle(title: String): List<Book>? {
        var books: List<Book>? = null
        viewModelScope.launch {
            books = repository.getBookByTitle(title)
        }
        return books
    }

    suspend fun getBookByAuthor(author: String): List<Book>? {
        var books: List<Book>? = null
        viewModelScope.launch {
            books = repository.getBookByAuthor(author)
        }
        return books
    }

    suspend fun getBookByPublisher(publisher: String): List<Book>? {
        var books: List<Book>? = null
        viewModelScope.launch {
            books = repository.getBookByPublisher(publisher)
        }
        return books
    }

    suspend fun getAllBooks(publisher: String): List<Book>? {
        var books: List<Book>? = null
        viewModelScope.launch {
            books = repository.getAllBooks()
        }
        return books
    }

    fun getLiveBookList(): LiveData<List<Book>> = LiveBookList

}