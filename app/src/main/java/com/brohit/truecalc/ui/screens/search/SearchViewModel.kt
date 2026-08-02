package com.brohit.truecalc.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.SearchRepository
import com.brohit.truecalc.domain.model.SearchRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    var query = mutableStateOf(TextFieldValue(""))
    private var searchJob: Job? = null

    private val _filteredRoutes = MutableStateFlow<List<SearchRoute>>(emptyList())
    val filteredRoutes = _filteredRoutes.asStateFlow()

    private val _filteredHistory = MutableStateFlow<List<CalculationHistory>>(emptyList())
    val filteredHistory = _filteredHistory.asStateFlow()

    private val _suggestedTools = MutableStateFlow<List<SearchRoute>>(emptyList())
    val suggestedTools = _suggestedTools.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val recentQueries: StateFlow<List<String>> = repository.getRecentQueries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSuggestions()
        filter(query.value)
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            repository.getMostUsedRoutes(5).collect { routes ->
                _suggestedTools.value = routes
            }
        }
    }

    fun filter(input: TextFieldValue) {
        query.value = input
        val text = input.text
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (text.isNotBlank()) {
                _isSearching.value = true
                delay(300L)
                launch {
                    repository.searchRoutes(text).collect { routes ->
                        _filteredRoutes.value = routes
                    }
                }
                launch {
                    repository.searchCalculationHistory(text).collect { history ->
                        _filteredHistory.value = history
                        _isSearching.value = false
                    }
                }
            } else {
                _isSearching.value = false
                _filteredRoutes.value = emptyList()
                _filteredHistory.value = emptyList()
            }
        }
    }

    fun addRecentQuery(q: String) {
        viewModelScope.launch {
            repository.addRecentQuery(q)
        }
    }

    fun removeRecentQuery(q: String) {
        viewModelScope.launch {
            repository.removeRecentQuery(q)
        }
    }

    fun clearRecentQueries() {
        viewModelScope.launch {
            repository.clearRecentQueries()
        }
    }
}
