package edu.dyds.movies.data.external.broker

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.remote.MovieDetailExternalSource

class MoviesBroker(
    private val tmdb: MovieDetailExternalSource,
    private val omdb: MovieDetailExternalSource,
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): RemoteMovie? {
        val tmdbResult = try {
            tmdb.getMovieByTitle(title)
        } catch (_: Exception) {
            null
        }

        val omdbResult = omdb.getMovieByTitle(title)

        return when {
            tmdbResult != null && omdbResult != null -> {
                combineResults(tmdbResult, omdbResult)
            }
            tmdbResult != null -> {
                tmdbResult.copy(overview = "TMDB:" + tmdbResult.overview)
            }
            omdbResult != null -> {
                omdbResult.copy(overview = "OMDB:" + omdbResult.overview)
            }
            else -> {
                null
            }
        }
    }

    private fun combineResults(tmdb: RemoteMovie, omdb: RemoteMovie): RemoteMovie {
        val overviewParts = listOfNotNull(
            tmdb.overview.takeIf { it.isNotBlank() }?.let { "TMDB: $it" },
            omdb.overview.takeIf { it.isNotBlank() }?.let { "OMDB: $it" },
        )
        return RemoteMovie(
            id = tmdb.id,
            title = tmdb.title.ifEmpty { omdb.title },
            overview = overviewParts.joinToString(" | "),
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

