package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.local.InMemoryMoviesLocalDataSource
import edu.dyds.movies.data.remote.RemoteMoviesDataSourceImpl
import edu.dyds.movies.data.repository.MoviesRepositoryImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import edu.dyds.movies.presentation.viewmodel.MoviesViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private const val API_KEY = "d18da1b5da16397619c688b0263cd281"

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
                    parameters.append("api_key", API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val localDataSource = InMemoryMoviesLocalDataSource()

    private val remoteDataSource = RemoteMoviesDataSourceImpl(tmdbHttpClient)

    @Composable
    fun getMoviesViewModel(): MoviesViewModel {
        return viewModel {
            val moviesRepository = MoviesRepositoryImpl(
                remoteDataSource = remoteDataSource,
                localDataSource = localDataSource
            )
            MoviesViewModel(
                getPopularMoviesUseCase = GetPopularMoviesUseCase(moviesRepository),
                getMovieDetailUseCase = GetMovieDetailUseCase(moviesRepository)
            )
        }
    }
}
