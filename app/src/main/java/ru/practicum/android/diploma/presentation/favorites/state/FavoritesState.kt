package ru.practicum.android.diploma.presentation.favorites.state

import ru.practicum.android.diploma.domain.models.Vacancy

sealed interface FavoritesState {
    object Loading : FavoritesState

    data class Content(val favoritesVacancies: List<Vacancy>) : FavoritesState

    data object Empty : FavoritesState

    data object Error : FavoritesState
}
