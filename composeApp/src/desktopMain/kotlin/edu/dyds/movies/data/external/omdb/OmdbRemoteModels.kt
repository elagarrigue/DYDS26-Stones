package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.domain.entity.Movie
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbRemoteMovie(
    @SerialName("Title") val title: String = "",
    @SerialName("Year") val year: String = "",
    @SerialName("Plot") val plot: String = "",
    @SerialName("Poster") val poster: String = "",
    @SerialName("Language") val language: String = "en",
    @SerialName("Released") val released: String = "",
    @SerialName("Metascore") val metascore: String = "0",
    @SerialName("imdbRating") val imdbRating: String = "0",
    @SerialName("imdbID") val imdbID: String = "",
    @SerialName("Response") val response: String = "False",
    @SerialName("Error") val error: String = "",
)

fun OmdbRemoteMovie.toDomainMovie(): Movie? {

    if (!response.equals("True", ignoreCase = true)) return null

    if (title.isBlank() || title.equals("N/A", ignoreCase = true)) return null
    if (imdbID.isBlank() || imdbID.equals("N/A", ignoreCase = true)) return null

    return Movie(
        id = imdbID.toMovieId(),
        title = title,
        overview = plot,
        releaseDate = year,
        poster = poster.takeUnless { it.equals("N/A", ignoreCase = true) }.orEmpty(),
        backdrop = null,
        originalTitle = title,
        originalLanguage = language,
        popularity = 0.0,
        voteAverage = imdbRating.toDoubleOrNull() ?: 0.0
    )
}

private fun String.toMovieId(): Int {
    val digits = substringAfter("tt").filter(Char::isDigit)
    return digits.toIntOrNull() ?: (hashCode() and Int.MAX_VALUE)
}

