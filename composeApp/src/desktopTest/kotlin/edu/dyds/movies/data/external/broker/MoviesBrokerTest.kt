package edu.dyds.movies.data.external.broker

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.data.external.MovieDetailExternalSource
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesBrokerTest {

    private fun tmdbMovie() = Movie(
        id = 27205,
        title = "Inception",
        overview = "",
        releaseDate = "2010-07-16",
        poster = "",
        backdrop = null,
        originalTitle = "Origen",
        originalLanguage = "",
        popularity = 10.0,
        voteAverage = 0.0,
    )

    private fun omdbMovie() = Movie(
        id = 1375666,
        title = "Inception",
        overview = "A thief who steals corporate secrets through the use of dream-sharing technology.",
        releaseDate = "2010",
        poster = "https://example.com/inception.jpg",
        backdrop = "/backdrop.jpg",
        originalTitle = "Inception",
        originalLanguage = "en",
        popularity = 80.0,
        voteAverage = 8.8,
    )

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getMovieDetails combines TMDB and OMDB results when both return data`() = runTest {
        val tmdb = mockk<MovieDetailExternalSource>()
        val omdb = mockk<MovieDetailExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        val tmdbCombinedMovie = tmdbMovie().copy(overview = "A science-fantasy heist set in dreams.")

        coEvery { tmdb.getMovieByTitle("Inception") } returns tmdbCombinedMovie
        coEvery { omdb.getMovieByTitle("Inception") } returns omdbMovie()

        val result = broker.getMovieByTitle("Inception")

        assertEquals(
            Movie(
                id = 27205,
                title = "Inception",
                overview = "TMDB: A science-fantasy heist set in dreams. | OMDB: A thief who steals corporate secrets through the use of dream-sharing technology.",
                releaseDate = "2010-07-16",
                poster = "https://example.com/inception.jpg",
                backdrop = "/backdrop.jpg",
                originalTitle = "Origen",
                originalLanguage = "en",
                popularity = 10.0,
                voteAverage = 8.8,
            ),
            result
        )
    }

    @Test
    fun `getMovieDetails returns TMDB result when OMDB does not return data`() = runTest {
        val tmdb = mockk<MovieDetailExternalSource>()
        val omdb = mockk<MovieDetailExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieByTitle("Inception") } returns tmdbMovie()
        coEvery { omdb.getMovieByTitle("Inception") } returns null

        val result = broker.getMovieByTitle("Inception")

        assertEquals(tmdbMovie().copy(overview = "TMDB: " + tmdbMovie().overview), result)
    }

    @Test
    fun `getMovieDetails returns OMDB result when TMDB does not return data`() = runTest {
        val tmdb = mockk<MovieDetailExternalSource>()
        val omdb = mockk<MovieDetailExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieByTitle("Inception") } returns null
        coEvery { omdb.getMovieByTitle("Inception") } returns omdbMovie()

        val result = broker.getMovieByTitle("Inception")

        assertEquals(omdbMovie().copy(overview = "OMDB: " + omdbMovie().overview), result)
    }

    @Test
    fun `getMovieDetails returns null when neither TMDB nor OMDB return data`() = runTest {
        val tmdb = mockk<MovieDetailExternalSource>()
        val omdb = mockk<MovieDetailExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieByTitle("Unknown") } returns null
        coEvery { omdb.getMovieByTitle("Unknown") } returns null

        val result = broker.getMovieByTitle("Unknown")

        assertNull(result)
    }
}


