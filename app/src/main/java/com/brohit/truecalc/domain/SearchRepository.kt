package com.brohit.truecalc.domain

import com.brohit.truecalc.domain.model.SearchRoute
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchRoutes(query: String): Flow<List<SearchRoute>>
}
