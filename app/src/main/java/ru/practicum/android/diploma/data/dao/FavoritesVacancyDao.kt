package ru.practicum.android.diploma.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.practicum.android.diploma.data.entity.FavoritesVacanciesEntity

@Dao
interface FavoritesVacancyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoritiesVacancy(vacancy: List<FavoritesVacanciesEntity>)

    @Query("DELETE FROM favorites_vacancies_table WHERE id = :vacancyID")
    suspend fun deleteVacancyFromFavorite(vacancyID: String)
}