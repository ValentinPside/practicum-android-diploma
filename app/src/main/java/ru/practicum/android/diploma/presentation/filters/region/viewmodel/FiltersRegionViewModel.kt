package ru.practicum.android.diploma.presentation.filters.region.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filters.models.FilterValue
import ru.practicum.android.diploma.domain.filters.models.ResponseStateArea
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.sharedpreferences.api.FiltersInteractor
import ru.practicum.android.diploma.presentation.filters.region.state.AreaViewState
import ru.practicum.android.diploma.presentation.filters.region.state.RegionSelectResult
import ru.practicum.android.diploma.ui.SingleLiveEvent
import ru.practicum.android.diploma.util.debounceFun
import javax.inject.Inject

class FiltersRegionViewModel @Inject constructor(
    private val countryId: String,
    private val filtersInteractor: FiltersInteractor
) : ViewModel() {

    private var queryString = ""

    private val state = MutableLiveData<AreaViewState>()

    private val onRegionClickDebounce =
        debounceFun<RegionSelectResult>(CLICK_DELAY, viewModelScope, false) { filterValue ->
            selectRegion.postValue(filterValue)
        }

    private val selectRegion = SingleLiveEvent<RegionSelectResult>()

    companion object {
        private const val CLICK_DELAY = 300L
    }

    init {
        loadAreas()
    }

    fun getSelectRegion(): SingleLiveEvent<RegionSelectResult> = selectRegion

    fun getState(): LiveData<AreaViewState> = state
    fun loadAreas() {
        viewModelScope.launch {
            asyncLoadData()
        }
    }

    suspend fun asyncLoadData() {
        state.postValue(AreaViewState.Loading)

        val response = filtersInteractor.getAreasByParentId(countryId)

        when (response) {
            is ResponseStateArea.ContentArea -> {
                val listResult = response.listAreas.filter {
                    if (queryString.isEmpty()) true else it.name.contains(queryString, true)
                }

                state.postValue(
                    if (listResult.isEmpty()) {
                        AreaViewState.Empty
                    } else {
                        AreaViewState.Content(listResult)
                    }
                )
            }

            is ResponseStateArea.Loading -> state.postValue(AreaViewState.Loading)

            else -> state.postValue(AreaViewState.Error)
        }
    }

    fun selectRegion(area: Area) {
        val parentArea = when (area.parentArea) {
            null -> area
            else -> area.parentArea
        }

        val result = RegionSelectResult(
            filterCountry = FilterValue(
                parentArea.id,
                "",
                parentArea.name,
                parentArea.name
            ),
            filterRegion = FilterValue(
                area.id,
                area.parentId,
                area.name,
                area.name
            )
        )

        onRegionClickDebounce(result)
    }

    fun search(query: String) {
        if (queryString == query) return
        queryString = query

        loadAreas()
    }
}
