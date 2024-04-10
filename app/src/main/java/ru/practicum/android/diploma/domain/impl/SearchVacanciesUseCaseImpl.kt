package ru.practicum.android.diploma.domain.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.dto.Vacancies
import javax.inject.Inject

class SearchVacanciesUseCaseImpl @Inject constructor(
    private val vacanciesRepository: VacanciesRepository
) : SearchVacanciesUseCase {
    override suspend fun invoke(query: String, page: Int): Vacancies {
        return withContext(Dispatchers.IO) {
            vacanciesRepository.search(query, page)
        }
    }
}