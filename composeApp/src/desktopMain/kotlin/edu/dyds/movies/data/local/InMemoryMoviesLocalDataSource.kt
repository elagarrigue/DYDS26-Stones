package edu.dyds.movies.data.local

import edu.dyds.movies.data.external.model.RemoteMovie

class InMemoryMoviesLocalDataSource {

    private val cachedMovies = mutableListOf<RemoteMovie>()

    fun getPopularMovies(): List<RemoteMovie> = cachedMovies.toList()

    fun savePopularMovies(movies: List<RemoteMovie>) {
        cachedMovies.clear()
        cachedMovies.addAll(movies)
    }

    fun getMovieDetail(id: Int): RemoteMovie? {
        return cachedMovies.firstOrNull { it.id == id }
    }

    fun saveMovieDetail(movie: RemoteMovie) {
        val index = cachedMovies.indexOfFirst { it.id == movie.id }
        if (index >= 0) {
            cachedMovies[index] = movie
        } else {
            cachedMovies.add(movie)
        }
    }
}

