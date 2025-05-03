package com.brohit.truecalc.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.domain.SearchRepository
import com.brohit.truecalc.domain.model.SearchRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    var query = mutableStateOf("")
    private var searchJob: Job? = null

    private val _allItems = MutableStateFlow<List<SearchRoute>>(emptyList())
    val allItems = _allItems.asStateFlow()

    init {
        filter("")
    }

    fun filter(input: String) {
        query.value = input
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300L)
            repository.searchRoutes(input).collect { routes ->
                _allItems.value = routes
            }
        }
    }
}
