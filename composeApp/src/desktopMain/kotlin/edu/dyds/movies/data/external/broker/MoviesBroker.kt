package edu.dyds.movies.data.external.broker

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.data.external.MovieDetailExternalSource

class MoviesBroker(
    private val tmdb: MovieDetailExternalSource,
    private val omdb: MovieDetailExternalSource,
) : MovieDetailExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie? {
        val tmdbResult: edu.dyds.movies.domain.entity.Movie? = tmdb.getMovieByTitle(title)
        val omdbResult: edu.dyds.movies.domain.entity.Movie? = omdb.getMovieByTitle(title)

        return when {
            tmdbResult != null && omdbResult != null -> {
                combineResults(tmdbResult, omdbResult)
            }
            tmdbResult != null -> {
                edu.dyds.movies.domain.entity.Movie(
                    id = tmdbResult.id,
                    title = tmdbResult.title,
                    overview = "TMDB: " + tmdbResult.overview,
                    releaseDate = tmdbResult.releaseDate,
                    poster = tmdbResult.poster,
                    backdrop = tmdbResult.backdrop,
                    originalTitle = tmdbResult.originalTitle,
                    originalLanguage = tmdbResult.originalLanguage,
                    popularity = tmdbResult.popularity,
                    voteAverage = tmdbResult.voteAverage,
                )
            }
            omdbResult != null -> {
                edu.dyds.movies.domain.entity.Movie(
                    id = omdbResult.id,
                    title = omdbResult.title,
                    overview = "OMDB: " + omdbResult.overview,
                    releaseDate = omdbResult.releaseDate,
                    poster = omdbResult.poster,
                    backdrop = omdbResult.backdrop,
                    originalTitle = omdbResult.originalTitle,
                    originalLanguage = omdbResult.originalLanguage,
                    popularity = omdbResult.popularity,
                    voteAverage = omdbResult.voteAverage,
                )
            }
            else -> {
                null
            }
        }
    }

    private fun combineResults(tmdb: Movie, omdb: Movie): Movie {
        val overviewParts = listOfNotNull(
            tmdb.overview.takeIf { it.isNotBlank() }?.let { "TMDB: $it" },
            omdb.overview.takeIf { it.isNotBlank() }?.let { "OMDB: $it" },
        )
        return Movie(
            id = tmdb.id,
            title = tmdb.title.ifEmpty { omdb.title },
            overview = overviewParts.joinToString(" | "),
            releaseDate = tmdb.releaseDate.ifEmpty { omdb.releaseDate },
            poster = tmdb.poster.ifEmpty { omdb.poster },
            backdrop = tmdb.backdrop ?: omdb.backdrop,
            originalTitle = tmdb.originalTitle.ifEmpty { omdb.originalTitle },
            originalLanguage = tmdb.originalLanguage.ifEmpty { omdb.originalLanguage },
            popularity = tmdb.popularity.takeUnless { it == 0.0 } ?: omdb.popularity,
            voteAverage = tmdb.voteAverage.takeUnless { it == 0.0 } ?: omdb.voteAverage,
        )
    }
}

