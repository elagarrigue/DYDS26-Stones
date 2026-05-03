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

	override suspend fun getMovieDetails(id: Int): RemoteMovie =
		tmdbHttpClient.get("/3/movie/$id").body()
}

