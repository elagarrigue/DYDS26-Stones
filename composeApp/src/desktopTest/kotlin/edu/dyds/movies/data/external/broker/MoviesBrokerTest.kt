package edu.dyds.movies.data.external.broker

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.remote.RemoteMoviesDataSource
import edu.dyds.movies.data.remote.SingleMovieExternalSource
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesBrokerTest {

    private fun tmdbMovie() = RemoteMovie(
        id = 27205,
        title = "Inception",
        overview = "",
        releaseDate = "2010-07-16",
        posterPath = "",
        backdropPath = null,
        originalTitle = "Origen",
        originalLanguage = "",
        popularity = 10.0,
        voteAverage = 0.0,
    )

    private fun omdbMovie() = RemoteMovie(
        id = 1375666,
        title = "Inception",
        overview = "A thief who steals corporate secrets through the use of dream-sharing technology.",
        releaseDate = "2010",
        posterPath = "https://example.com/inception.jpg",
        backdropPath = "/backdrop.jpg",
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
        val tmdb = mockk<RemoteMoviesDataSource>()
        val omdb = mockk<SingleMovieExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieDetails("Inception") } returns tmdbMovie()
        coEvery { omdb.getMovieByTitle("Inception") } returns omdbMovie()

        val result = broker.getMovieDetails("Inception")

        assertEquals(
            RemoteMovie(
                id = 27205,
                title = "Inception",
                overview = "A thief who steals corporate secrets through the use of dream-sharing technology. TMDB+OMDB",
                releaseDate = "2010-07-16",
                posterPath = "https://example.com/inception.jpg",
                backdropPath = "/backdrop.jpg",
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
        val tmdb = mockk<RemoteMoviesDataSource>()
        val omdb = mockk<SingleMovieExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieDetails("Inception") } returns tmdbMovie()
        coEvery { omdb.getMovieByTitle("Inception") } returns null

        val result = broker.getMovieDetails("Inception")

        assertEquals(tmdbMovie().copy(overview = " TMDB"), result)
    }

    @Test
    fun `getMovieDetails returns OMDB result when TMDB does not return data`() = runTest {
        val tmdb = mockk<RemoteMoviesDataSource>()
        val omdb = mockk<SingleMovieExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieDetails("Inception") } throws Exception("not found")
        coEvery { omdb.getMovieByTitle("Inception") } returns omdbMovie()

        val result = broker.getMovieDetails("Inception")

        assertEquals(omdbMovie().copy(overview = "A thief who steals corporate secrets through the use of dream-sharing technology. OMDB"), result)
    }

    @Test
    fun `getMovieDetails throws when neither TMDB nor OMDB return data`() = runTest {
        val tmdb = mockk<RemoteMoviesDataSource>()
        val omdb = mockk<SingleMovieExternalSource>()
        val broker = MoviesBroker(tmdb = tmdb, omdb = omdb)

        coEvery { tmdb.getMovieDetails("Unknown") } throws Exception("not found")
        coEvery { omdb.getMovieByTitle("Unknown") } returns null

        val exception = assertFailsWith<Exception> {
            broker.getMovieDetails("Unknown")
        }

        assertEquals("Movie not found: Unknown", exception.message)
    }
}


