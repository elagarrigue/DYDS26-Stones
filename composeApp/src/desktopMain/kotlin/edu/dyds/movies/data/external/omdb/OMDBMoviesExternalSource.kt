package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.external.MovieDetailExternalSource
import edu.dyds.movies.domain.entity.Movie
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OMDBMoviesExternalSource(
    private val apiKey: String,
    private val clientProvider: () -> HttpClient = { createHttpClient(apiKey) },
    private val movieResponseProvider: suspend (HttpClient, String) -> OmdbRemoteMovie = { client, title ->
        client.get("/") {
            url {
                parameters.append("t", title)
            }
        }.body()
    },
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie? {
        val response = movieResponseProvider(clientProvider(), title)
        return response.toDomainMovie()
    }
}

private fun createHttpClient(apiKey: String): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTPS
            host = "www.omdbapi.com"
            parameters.append("apikey", apiKey)
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
    }
}
