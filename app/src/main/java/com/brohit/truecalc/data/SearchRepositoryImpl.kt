package com.brohit.truecalc.data

import com.brohit.truecalc.domain.SearchRepository
import com.brohit.truecalc.domain.model.SearchRoute
import com.brohit.truecalc.ui.screens.home.calculatorsWithCategories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepositoryImpl : SearchRepository {

    private val routes = calculatorsWithCategories.flatMap { item ->
        item.calculators.map {
            SearchRoute(
                route = it.route,
                category = item.category,
                label = it.name,
                description = it.description,
                icon = it.icon
            )
        }
    }


    override fun searchRoutes(query: String): Flow<List<SearchRoute>> = flow {
        val trimmedQuery = query.trim().lowercase()
        if (trimmedQuery.isEmpty()) {
            emit(routes)
        } else {
            emit(
                routes.filter {
                    it.label.lowercase().contains(trimmedQuery)
                }
            )
        }
    }
}
