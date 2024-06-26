package ru.practicum.android.diploma.presentation.filters.region.state

sealed interface FiltersWorkPlaceViewState {
    data class Content(
        val country: String,
        val region: String,
        val filterChanged: Boolean
    ) : FiltersWorkPlaceViewState

    object Empty : FiltersWorkPlaceViewState
}
