package edu.dyds.movies.data.repository

import edu.dyds.movies.data.external.mapper.toDomainMovie
import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.external.model.RemoteResult
import edu.dyds.movies.data.local.InMemoryMoviesLocalDataSource
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TmdbMoviesRepository(
	private val tmdbHttpClient: HttpClient,
	private val localDataSource: InMemoryMoviesLocalDataSource,
) : MoviesRepository {

	override suspend fun getAllMovies(): List<Movie> {
		val cachedMovies = localDataSource.getPopularMovies()
		val remoteMovies = if (cachedMovies.isNotEmpty()) {
			cachedMovies
		} else {
			try {
				getTMDBPopularMovies().results.also { localDataSource.savePopularMovies(it) }
			} catch (_: Exception) {
				emptyList()
			}
		}

		return remoteMovies.map(RemoteMovie::toDomainMovie)
	}

	override suspend fun getMovieDetail(id: Int): Movie? {
		val cachedMovie = localDataSource.getMovieDetail(id)
		if (cachedMovie != null) return cachedMovie.toDomainMovie()

		return try {
			getTMDBMovieDetails(id).also(localDataSource::saveMovieDetail).toDomainMovie()
		} catch (_: Exception) {
			null
		}
	}

	private suspend fun getTMDBMovieDetails(id: Int): RemoteMovie =
		tmdbHttpClient.get("/3/movie/$id").body()

	private suspend fun getTMDBPopularMovies(): RemoteResult =
		tmdbHttpClient.get("/3/discover/movie?sort_by=popularity.desc").body()
}

