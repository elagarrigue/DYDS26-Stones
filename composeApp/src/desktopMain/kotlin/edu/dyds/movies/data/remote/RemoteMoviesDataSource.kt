package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.external.model.RemoteResult

interface RemoteMoviesDataSource {
	suspend fun getPopularMovies(): RemoteResult
	suspend fun getMovieDetails(id: Int): RemoteMovie
}

