package com.example.drivesense

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    val allRecords: LiveData<List<Record>> = repository.allRecords.asLiveData()

    fun insert(record: Record) = viewModelScope.launch {
        repository.insert(record)
    }
}

class HistoryViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}