package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.model.RemoteMovie

interface SingleMovieExternalSource {
    suspend fun getMovieByTitle(title: String): RemoteMovie?
}

