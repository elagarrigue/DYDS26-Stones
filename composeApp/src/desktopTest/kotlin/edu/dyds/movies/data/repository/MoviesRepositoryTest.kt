package edu.dyds.movies.data.repository

import edu.dyds.movies.data.external.model.RemoteMovie
import edu.dyds.movies.data.external.model.RemoteResult
import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.data.remote.RemoteMoviesDataSource
import edu.dyds.movies.domain.entity.Movie
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesRepositoryTest {

    private val remoteDataSource = mockk<RemoteMoviesDataSource>()
    private val localDataSource = mockk<MoviesLocalDataSource>()

    private val repository = MoviesRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource
    )

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    private fun testMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        overview = "Overview $id",
        releaseDate = "2024-01-01",
        poster = "https://example.com/poster$id.jpg",
        backdrop = "https://example.com/backdrop$id.jpg",
        originalTitle = "Original $id",
        originalLanguage = "en",
        popularity = 75.5,
        voteAverage = 8.5
    )

    private fun testRemoteMovie(id: Int) = RemoteMovie(
        id = id,
        title = "Movie $id",
        overview = "Overview $id",
        releaseDate = "2024-01-01",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        originalTitle = "Original $id",
        originalLanguage = "en",
        popularity = 75.5,
        voteAverage = 8.5
    )

    @org.junit.Test
    fun `getAllMovies returns cached movies`() = runTest {
        val cached = listOf(testMovie(1), testMovie(2))
        every { localDataSource.getPopularMovies() } returns cached

        val result = repository.getAllMovies()

        assertEquals(cached, result)
    }

    @org.junit.Test
    fun `getAllMovies fetches from remote when cache is empty`() = runTest {
        val remoteMovies = listOf(testRemoteMovie(1), testRemoteMovie(2))
        val remoteResult = RemoteResult(page = 1, results = remoteMovies, totalPages = 1, totalResults = remoteMovies.size)

        every { localDataSource.getPopularMovies() } returns emptyList()
        coEvery { remoteDataSource.getPopularMovies() } returns remoteResult
        every { localDataSource.savePopularMovies(any()) } returns Unit

        val result = repository.getAllMovies()

        coVerify { localDataSource.savePopularMovies(match { it.size == 2 }) }
        assertTrue(result.isNotEmpty())
        assertEquals(2, result.size)
    }

    @org.junit.Test
    fun `getAllMovies returns empty list when cache is empty and remote fails`() = runTest {
        every { localDataSource.getPopularMovies() } returns emptyList()
        coEvery { remoteDataSource.getPopularMovies() } throws Exception("Network error")

        val result = repository.getAllMovies()

        assertTrue(result.isEmpty())
    }

    @org.junit.Test
    fun `getMovieDetail returns cached detail`() = runTest {
        val cached = testMovie(5)
        every { localDataSource.getMovieDetail(5) } returns cached

        val result = repository.getMovieDetail(5)

        assertEquals(cached, result)
    }

    @org.junit.Test
    fun `getMovieDetail fetches from remote when not cached`() = runTest {
        val remoteMovie = testRemoteMovie(10)
        val cachedList = listOf(testMovie(1), testMovie(10))

        every { localDataSource.getMovieDetail(10) } returns null
        coEvery { remoteDataSource.getMovieDetails(10) } returns remoteMovie
        every { localDataSource.getPopularMovies() } returns cachedList
        every { localDataSource.saveMovieDetail(any(), any()) } returns Unit

        val result = repository.getMovieDetail(10)

        coVerify {localDataSource.saveMovieDetail(match { it.id == 10 }, any()) }
        assertNotNull(result)
        assertEquals(10, result.id)
    }

    @org.junit.Test
    fun `getMovieDetail returns correct movie when not in cache`() = runTest {
        val cachedList = listOf(testMovie(1), testMovie(2))
        val remoteMovie = testRemoteMovie(10)

        every { localDataSource.getMovieDetail(10) } returns null
        coEvery { remoteDataSource.getMovieDetails(10) } returns remoteMovie
        every { localDataSource.getPopularMovies() } returns cachedList
        every { localDataSource.saveMovieDetail(any(), any()) } returns Unit

        val result = repository.getMovieDetail(10)

        coVerify {localDataSource.saveMovieDetail(match { it.id == 10 }, any()) }
        assertNotNull(result)
        assertEquals(10, result.id)
        assertEquals("Movie 10", result.title)
    }

    @org.junit.Test
    fun `getMovieDetail returns null when not cached and remote fails`() = runTest {
        every { localDataSource.getMovieDetail(999) } returns null
        coEvery { remoteDataSource.getMovieDetails(999) } throws Exception("Not found")

        val result = repository.getMovieDetail(999)

        assertNull(result)
    }
}