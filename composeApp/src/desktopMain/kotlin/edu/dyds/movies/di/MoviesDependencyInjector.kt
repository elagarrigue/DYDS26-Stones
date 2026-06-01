package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.external.tmdb.TMDBMoviesExternalSource
import edu.dyds.movies.data.external.omdb.OMDBMoviesExternalSource
import edu.dyds.movies.data.external.broker.MoviesBroker
import edu.dyds.movies.data.local.MoviesLocalDataSourceImpl
import edu.dyds.movies.data.repository.MoviesRepositoryImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCaseImpl
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCaseImpl
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private val TMDB_API_KEY: String = System.getenv("TMDB_API_KEY")
    ?: "d18da1b5da16397619c688b0263cd281"

object MoviesDependencyInjector {

    private val tmdbHttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.themoviedb.org"
                    parameters.append("api_key", TMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val localDataSource = MoviesLocalDataSourceImpl()

    private val tmdbRemoteSource = TMDBMoviesExternalSource(tmdbHttpClient)

    private val omdbApiKey: String
        get() {
            val envKey = System.getenv("OMDB_API_KEY")
            if (envKey != null) return envKey
            val env = System.getenv("APP_ENV") ?: "development"
            return if (env == "development") {
                "a96e7f78"
            } else {
                error("OMDB_API_KEY environment variable is not set")
            }
        }

    private val omdbRemoteSource = OMDBMoviesExternalSource(apiKey = omdbApiKey)

      private val popularMoviesExternalSource = tmdbRemoteSource
      private val movieDetailExternalSource = MoviesBroker(
        tmdb = tmdbRemoteSource,
        omdb = omdbRemoteSource,
      )

    @Composable
    fun getHomeViewModel(): HomeViewModel {
        return viewModel {
            val moviesRepository = MoviesRepositoryImpl(
            popularMoviesExternalSource = popularMoviesExternalSource,
            movieDetailExternalSource = movieDetailExternalSource,
                localDataSource = localDataSource
            )
            HomeViewModel(
                getPopularMoviesUseCase = GetPopularMoviesUseCaseImpl(moviesRepository)
            )
        }
    }

    @Composable
    fun getDetailViewModel(): DetailViewModel {
        return viewModel {
            val moviesRepository = MoviesRepositoryImpl(
            popularMoviesExternalSource = popularMoviesExternalSource,
            movieDetailExternalSource = movieDetailExternalSource,
                localDataSource = localDataSource
            )
            DetailViewModel(
                getMovieDetailUseCase = GetMovieDetailUseCaseImpl(moviesRepository)
            )
        }
    }
}
