package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entity.Movie

class InMemoryMoviesLocalDataSource : MoviesLocalDataSource {

	private val cachedMovies = mutableListOf<Movie>()

	override fun getPopularMovies(): List<Movie> = cachedMovies.toList()

	override fun savePopularMovies(movies: List<Movie>) {
		cachedMovies.clear()
		cachedMovies.addAll(movies)
	}

	override fun getMovies(): List<Movie> {
		return cachedMovies
	}

	override fun getMovieDetail(id: Int): Movie? {
		return cachedMovies.firstOrNull { it.id == id }
	}

	override fun saveMovieDetail(movie: Movie, index: Int) {
		cachedMovies.add(index, movie)
	}
}

