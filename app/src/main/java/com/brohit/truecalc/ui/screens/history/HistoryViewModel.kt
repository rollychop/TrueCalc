package com.brohit.truecalc.ui.screens.history

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.domain.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    val groupedHistory: StateFlow<Map<String, List<CalculationHistory>>> = repository.getAllHistory()
        .map { history ->
            history.groupBy { it.toolName }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val expandedTools = mutableStateListOf<String>()

    fun toggleExpansion(toolName: String) {
        if (expandedTools.contains(toolName)) {
            expandedTools.remove(toolName)
        } else {
            expandedTools.add(toolName)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }
}
