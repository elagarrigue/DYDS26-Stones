package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.tmdb.RemoteResult

interface PopularMoviesExternalSource {
	suspend fun getPopularMovies(): RemoteResult
}


