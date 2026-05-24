package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.external.model.RemoteResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteMoviesDataSourceImpl(
	private val tmdbHttpClient: HttpClient,
) : RemoteMoviesDataSource {

	override suspend fun getPopularMovies(): RemoteResult =
		tmdbHttpClient.get("/3/discover/movie?sort_by=popularity.desc").body()

	override suspend fun getMovieDetails(title: String): RemoteMovie {
		val searchResult = searchMovieByTitle(title)
		val movieId = requireFirstMovieId(searchResult, title)
		return fetchMovieDetails(movieId)
	}

	private suspend fun searchMovieByTitle(title: String): RemoteResult =
		tmdbHttpClient.get("/3/search/movie?query=$title").body()

	private fun requireFirstMovieId(searchResult: RemoteResult, title: String): Int =
		searchResult.results.firstOrNull()?.id
			?: throw Exception("Movie not found: $title")

	private suspend fun fetchMovieDetails(movieId: Int): RemoteMovie =
		tmdbHttpClient.get("/3/movie/$movieId").body()
}

