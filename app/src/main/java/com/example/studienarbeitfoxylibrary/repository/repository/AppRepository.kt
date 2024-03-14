package com.example.studienarbeitfoxylibrary.repository.repository

import android.app.Application
import com.example.studienarbeitfoxylibrary.repository.database.Book
import com.example.studienarbeitfoxylibrary.repository.database.BookDao
import com.example.studienarbeitfoxylibrary.repository.database.BookDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(application: Application) {
    private val bookDao: BookDao

    init {
        val db = BookDataBase.createInstance(application)
        bookDao = db.bookDao
    }

    //Implement all Methods:
    suspend fun insert(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.insert(book)
        }
    }

    suspend fun update(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.update(book)
        }
    }

    suspend fun delete(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.delete(book)
        }
    }

    suspend fun getBookById(bookId: Long): Book? {
        var book: Book? = null
        withContext(Dispatchers.IO) {
            book = bookDao.getBookById(bookId)
        }
        return book
    }

    suspend fun getAllBooks(): List<Book>? {
        var books: List<Book>? = null
        withContext(Dispatchers.IO) {
            books = bookDao.getBookList()
        }
        return books
    }

    suspend fun getBookByIsbn(isbn: String): Book? {
        var book: Book? = null
        withContext(Dispatchers.IO) {
            book = bookDao.getBookByIsbn(isbn)
        }
        return book
    }

    suspend fun getBookByTitle(title: String): List<Book>? {
        var books: List<Book>? = null
        withContext(Dispatchers.IO) {
            books = bookDao.getBookByTitle(title)
        }
        return books
    }

    suspend fun getBookByAuthor(author: String): List<Book>? {
        var books: List<Book>? = null
        withContext(Dispatchers.IO) {
            books = bookDao.getBookByAuthor(author)
        }
        return books
    }

    suspend fun getBookByPublisher(publisher: String): List<Book>? {
        var books: List<Book>? = null
        withContext(Dispatchers.IO) {
            books = bookDao.getBookByPublisher(publisher)
        }
        return books
    }

    fun getLiveDataBooks() = bookDao.getLiveDataBookList()


}