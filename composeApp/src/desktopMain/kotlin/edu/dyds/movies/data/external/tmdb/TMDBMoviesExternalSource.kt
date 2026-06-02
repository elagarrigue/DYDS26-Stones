package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.data.external.MovieDetailExternalSource
import edu.dyds.movies.data.external.PopularMoviesExternalSource
import edu.dyds.movies.data.external.tmdb.mapper.toDomainMovie
import edu.dyds.movies.domain.entity.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class TMDBMoviesExternalSource(
	private val tmdbHttpClient: HttpClient,
) : PopularMoviesExternalSource, MovieDetailExternalSource {

	override suspend fun getPopularMovies(): RemoteResult =
		tmdbHttpClient.get("/3/discover/movie?sort_by=popularity.desc").body()

	override suspend fun getMovieByTitle(title: String): Movie? {
		val searchResult = searchMovieByTitle(title)
		val movieId = searchResult.results.firstOrNull()?.id ?: return null
		return fetchMovieDetails(movieId).toDomainMovie()
	}

	private suspend fun searchMovieByTitle(title: String): RemoteResult =
		tmdbHttpClient.get("/3/search/movie?query=$title").body()


	private suspend fun fetchMovieDetails(movieId: Int): RemoteMovie =
		tmdbHttpClient.get("/3/movie/$movieId").body()
}


