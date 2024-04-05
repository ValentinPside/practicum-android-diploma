package ru.practicum.android.diploma.di.app

import dagger.Module
import dagger.Provides
import ru.practicum.android.diploma.domain.favorites.api.FavoritesRepository
import ru.practicum.android.diploma.data.favorites.impl.FavoritesRepositoryImpl

@Module
object FavoriteRepositoryModule {
    @Provides
    fun providesFavoritiesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository = impl

}