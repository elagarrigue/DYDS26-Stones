package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.external.model.RemoteResult

interface PopularMoviesExternalSource {
	suspend fun getPopularMovies(): RemoteResult
}

interface MovieDetailExternalSource {
	suspend fun getMovieByTitle(title: String): RemoteMovie?
}

