package edu.dyds.movies.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
) : ViewModel() {

    private val homeStateMutableStateFlow = MutableStateFlow(HomeUiState())

    val homeStateFlow: Flow<HomeUiState> = homeStateMutableStateFlow

    fun getAllMovies() {
        viewModelScope.launch {
            homeStateMutableStateFlow.emit(HomeUiState(isLoading = true))
            homeStateMutableStateFlow.emit(
                HomeUiState(
                    isLoading = false,
                    movies = getPopularMoviesUseCase()
                )
            )
        }
    }

    data class HomeUiState(
        val isLoading: Boolean = false,
        val movies: List<QualifiedMovie> = emptyList(),
    )
}