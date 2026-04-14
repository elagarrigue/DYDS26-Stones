package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class GetPopularMoviesUseCase(
	private val moviesRepository: MoviesRepository,
) {
	suspend operator fun invoke(): List<Movie> = moviesRepository.getAllMovies()
}

