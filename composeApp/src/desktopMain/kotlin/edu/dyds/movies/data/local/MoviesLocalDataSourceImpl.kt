package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entity.Movie

class MoviesLocalDataSourceImpl : MoviesLocalDataSource {

	private val cachedPopularMovies = mutableListOf<Movie>()
	private val cachedMovieDetails = mutableMapOf<Int, Movie>()

	override fun getPopularMovies(): List<Movie> = cachedPopularMovies.toList()

	override fun savePopularMovies(movies: List<Movie>) {
		cachedPopularMovies.clear()
		cachedPopularMovies.addAll(movies)
		cachedMovieDetails.clear()
	}


	override fun getMovieDetail(id: Int): Movie? {
		return cachedMovieDetails[id]
	}

	override fun saveMovieDetail(movie: Movie, index: Int) {
		cachedMovieDetails[movie.id] = movie

		val existingIndex = cachedPopularMovies.indexOfFirst { it.id == movie.id }
		if (existingIndex >= 0) {
			cachedPopularMovies[existingIndex] = movie
			return
		}

		cachedPopularMovies.add(index, movie)
	}
}

