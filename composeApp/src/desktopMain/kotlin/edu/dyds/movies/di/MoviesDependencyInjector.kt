package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.local.InMemoryMoviesLocalDataSource
import edu.dyds.movies.data.remote.RemoteMoviesDataSourceImpl
import edu.dyds.movies.data.repository.MoviesRepositoryImpl
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCaseImpl
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCaseImpl
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel
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
    fun getHomeViewModel(): HomeViewModel {
        return viewModel {
            val moviesRepository = MoviesRepositoryImpl(
                remoteDataSource = remoteDataSource,
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
                remoteDataSource = remoteDataSource,
                localDataSource = localDataSource
            )
            DetailViewModel(
                getMovieDetailUseCase = GetMovieDetailUseCaseImpl(moviesRepository)
            )
        }
    }
}
