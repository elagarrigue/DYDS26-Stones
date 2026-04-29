package edu.dyds.movies.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
) : ViewModel() {

    private val detailUiState = MutableStateFlow(DetailUiState())

    val movieDetailStateFlow: StateFlow<DetailUiState> = detailUiState.asStateFlow()

    fun getMovieDetail(id: Int) {
        viewModelScope.launch {
            val loadingState = DetailUiState(isLoading = true)
            detailUiState.value = loadingState

            val loadedState = DetailUiState(
                isLoading = false,
                movie = getMovieDetailUseCase(id)
            )
            detailUiState.value = loadedState
        }
    }

    data class DetailUiState(
        val isLoading: Boolean = false,
        val movie: Movie? = null,
    )
}
