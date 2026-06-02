package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.tmdb.RemoteMovie

interface MovieDetailExternalSource {
    suspend fun getMovieByTitle(title: String): RemoteMovie?
}

