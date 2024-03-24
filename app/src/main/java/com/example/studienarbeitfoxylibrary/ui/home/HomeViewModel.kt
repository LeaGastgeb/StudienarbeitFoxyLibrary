package com.example.studienarbeitfoxylibrary.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studienarbeitfoxylibrary.repository.database.Book
import com.example.studienarbeitfoxylibrary.repository.repository.AppRepository

class HomeViewModel(application: Application): AndroidViewModel(application)
{

    ////////////////////////////////////////////////////////////////
    //Repository
    private val repository = AppRepository(application)
    private var liveBookList = repository.getLiveDataBooks()
    ////////////////////////////////////////////////////////////////

    fun getLiveDataBooks(): LiveData<List<Book>> {
        return liveBookList
    }
}