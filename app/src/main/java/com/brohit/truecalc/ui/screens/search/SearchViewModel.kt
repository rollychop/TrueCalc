package com.brohit.truecalc.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.SearchRepository
import com.brohit.truecalc.domain.UsageRepository
import com.brohit.truecalc.ui.tools.ToolRegistry
import com.brohit.truecalc.ui.tools.ToolSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    usageRepository: UsageRepository,
) : ViewModel() {

    var query = mutableStateOf(TextFieldValue(""))
    private var searchJob: Job? = null

    /** Tools matching the query, best match first ([ToolRegistry.search]). */
    private val _filteredTools = MutableStateFlow<List<ToolSpec>>(emptyList())
    val filteredTools = _filteredTools.asStateFlow()

    private val _filteredHistory = MutableStateFlow<List<CalculationHistory>>(emptyList())
    val filteredHistory = _filteredHistory.asStateFlow()

    /** Most used tools, or the first few registered tools before anything has been used. */
    val suggestedTools: StateFlow<List<ToolSpec>> = usageRepository.getMostUsed(SUGGESTION_QUERY_LIMIT)
        .map { routes ->
            ToolRegistry.forRouteKeys(routes, SUGGESTION_LIMIT)
                .ifEmpty { ToolRegistry.all.take(SUGGESTION_LIMIT) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ToolRegistry.all.take(SUGGESTION_LIMIT)
        )

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    val recentQueries: StateFlow<List<String>> = repository.getRecentQueries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        filter(query.value)
    }

    fun filter(input: TextFieldValue) {
        query.value = input
        val text = input.text
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (text.isNotBlank()) {
                _isSearching.value = true
                delay(300L)
                _filteredTools.value = ToolRegistry.search(text)
                repository.searchCalculationHistory(text).collect { history ->
                    _filteredHistory.value = history
                    _isSearching.value = false
                }
            } else {
                _isSearching.value = false
                _filteredTools.value = emptyList()
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

    private companion object {
        const val SUGGESTION_LIMIT = 5
        const val SUGGESTION_QUERY_LIMIT = 10
    }
}
