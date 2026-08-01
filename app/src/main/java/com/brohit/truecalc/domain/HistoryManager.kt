package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.entity.CalculationHistory
import com.brohit.truecalc.ui.navigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryManager @Inject constructor(
    private val repository: HistoryRepository,
    private val scope: CoroutineScope
) {
    private val debounceJobs = mutableMapOf<String, Job>()

    fun log(
        route: Route,
        name: String,
        inputs: Map<String, String>,
        results: Map<String, String>,
        restorationInputs: Map<String, String> = emptyMap()
    ) {
        val routeStr = Json.encodeToString(route)
        
        // Cancel existing job for this tool to debounce
        debounceJobs[routeStr]?.cancel()
        
        debounceJobs[routeStr] = scope.launch {
            delay(2000) // Wait for user to stop typing
            
            val history = CalculationHistory(
                toolRoute = routeStr,
                toolName = name,
                inputsJson = Json.encodeToString(inputs),
                resultsJson = Json.encodeToString(results),
                restorationJson = Json.encodeToString(restorationInputs)
            )
            repository.addHistory(history)
            debounceJobs.remove(routeStr)
        }
    }
}
