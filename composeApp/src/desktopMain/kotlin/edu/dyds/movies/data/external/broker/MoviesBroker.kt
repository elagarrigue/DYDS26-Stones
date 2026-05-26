package edu.dyds.movies.data.external.broker

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.external.model.RemoteResult
import edu.dyds.movies.data.remote.RemoteMoviesDataSource
import edu.dyds.movies.data.remote.SingleMovieExternalSource

class MoviesBroker(
    private val tmdb: RemoteMoviesDataSource,
    private val omdb: SingleMovieExternalSource,
) : RemoteMoviesDataSource {

    override suspend fun getPopularMovies(): RemoteResult {
        return tmdb.getPopularMovies()
    }

    override suspend fun getMovieDetails(title: String): RemoteMovie {
        val tmdbResult = try {
            tmdb.getMovieDetails(title)
        } catch (e: Exception) {
            null
        }

        val omdbResult = omdb.getMovieByTitle(title)

        return when {
            tmdbResult != null && omdbResult != null -> {
                // Combinar ambos resultados - TMDB tiene precedencia
                combineResults(tmdbResult, omdbResult)
            }
            tmdbResult != null -> {
                // Solo TMDB retorna resultado
                tmdbResult.copy(overview = tmdbResult.overview + " TMDB")
            }
            omdbResult != null -> {
                // Solo OMDB retorna resultado
                omdbResult.copy(overview = omdbResult.overview + " OMDB")
            }
            else -> {
                // Ninguno retorna resultado
                throw Exception("Movie not found: $title")
            }
        }
    }

    private fun combineResults(tmdb: RemoteMovie, omdb: RemoteMovie): RemoteMovie {
        return RemoteMovie(
            id = tmdb.id,
            title = tmdb.title.ifEmpty { omdb.title },
            overview = (tmdb.overview.takeUnless { it.isEmpty() } ?: omdb.overview) + " TMDB+OMDB",
            releaseDate = tmdb.releaseDate.ifEmpty { omdb.releaseDate },
            posterPath = tmdb.posterPath.ifEmpty { omdb.posterPath },
            backdropPath = tmdb.backdropPath ?: omdb.backdropPath,
            originalTitle = tmdb.originalTitle.ifEmpty { omdb.originalTitle },
            originalLanguage = tmdb.originalLanguage.ifEmpty { omdb.originalLanguage },
            popularity = tmdb.popularity.takeUnless { it == 0.0 } ?: omdb.popularity,
            voteAverage = tmdb.voteAverage.takeUnless { it == 0.0 } ?: omdb.voteAverage,
        )
    }
}

