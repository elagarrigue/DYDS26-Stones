package edu.dyds.movies.data.repository

import edu.dyds.movies.data.external.mapper.toDomainMovie
import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.data.remote.RemoteMoviesDataSource
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
	private val remoteDataSource: RemoteMoviesDataSource,
	private val localDataSource: MoviesLocalDataSource,
) : MoviesRepository {

	override suspend fun getAllMovies(): List<Movie> {
		val cachedMovies = localDataSource.getPopularMovies()
		val remoteMovies = if (cachedMovies.isNotEmpty()) {
			cachedMovies
		} else {
			try {
				remoteDataSource.getPopularMovies().results.map { it.toDomainMovie() }
					.also { localDataSource.savePopularMovies(it) }
			} catch (_: Exception) {
				emptyList()
			}
		}
		return remoteMovies
	}

	override suspend fun getMovieDetail(id: Int): Movie? {
		val cachedMovieDetail = localDataSource.getMovieDetail(id)

		val remoteDetail= if (cachedMovieDetail != null) {
			cachedMovieDetail
		}
		else try {
			val cachedMovie = getAllMovies()
			val movieTitle = cachedMovie.firstOrNull { it.id == id }?.title

			movieTitle?.let {
				remoteDataSource.getMovieDetails(it).toDomainMovie()
					.also { movie -> saveMovieDetail(movie) }
			}
            } catch (_: Exception) {
				null
            }
		return remoteDetail
	}

	private suspend fun saveMovieDetail(movie: Movie) {

		val cachedMovie = getAllMovies()
		val index = cachedMovie.indexOfFirst { it.id == movie.id }

		if (index >= 0) {
			localDataSource.saveMovieDetail(movie, index)
		} else {
			localDataSource.saveMovieDetail(movie, cachedMovie.size)
		}
	}

}

