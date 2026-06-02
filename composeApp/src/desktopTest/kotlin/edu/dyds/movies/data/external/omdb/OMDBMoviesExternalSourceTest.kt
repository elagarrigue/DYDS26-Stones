package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.external.tmdb.RemoteMovie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import io.mockk.mockk
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class OMDBMoviesExternalSourceTest {

    private fun createSource(responseProvider: suspend (String) -> OmdbRemoteMovie): OMDBMoviesExternalSource {
        return OMDBMoviesExternalSource(
            apiKey = "test-key",
            clientProvider = { mockk(relaxed = true) },
            movieResponseProvider = { _, title -> responseProvider(title) }
        )
    }

    @Test
    fun `getMovieByTitle returns mapped movie when OMDB finds a result`() = runTest {
        val source = createSource { title ->
            assertEquals("Inception", title)
            OmdbRemoteMovie(
                title = "Inception",
                year = "2010",
                plot = "A thief who steals corporate secrets through the use of dream-sharing technology.",
                poster = "https://example.com/inception.jpg",
                language = "en",
                released = "2010-07-16",
                metascore = "74",
                imdbRating = "8.8",
                imdbID = "tt1375666",
                response = "True"
            )
        }

        val result = source.getMovieByTitle("Inception")

        assertEquals(
            RemoteMovie(
                id = 1375666,
                title = "Inception",
                overview = "A thief who steals corporate secrets through the use of dream-sharing technology.",
                releaseDate = "2010",
                posterPath = "https://example.com/inception.jpg",
                backdropPath = null,
                originalTitle = "Inception",
                originalLanguage = "en",
                popularity = 0.0,
                voteAverage = 8.8
            ),
            result
        )
    }

    @Test
    fun `getMovieByTitle returns null when OMDB does not find a result`() = runTest {
        val source = createSource { title ->
            assertEquals("Unknown Movie", title)
            OmdbRemoteMovie(
                response = "False"
            )
        }

        val result = source.getMovieByTitle("Unknown Movie")

        assertNull(result)
    }

    @Test
    fun `getMovieByTitle propagates network errors`() = runTest {
        val source = OMDBMoviesExternalSource(
            apiKey = "test-key",
            clientProvider = { mockk(relaxed = true) },
            movieResponseProvider = { _, _ ->
            throw IOException("timeout")
            }
        )

        assertFailsWith<IOException> {
            source.getMovieByTitle("Inception")
        }
    }
}


