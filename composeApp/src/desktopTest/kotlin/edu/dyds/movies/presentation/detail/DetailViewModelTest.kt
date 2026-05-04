package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailUseCase
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val useCase = mockk<GetMovieDetailUseCase>(relaxed = true)

    private lateinit var viewModel: DetailViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = DetailViewModel(useCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private suspend fun state(): DetailViewModel.DetailUiState = viewModel.movieDetailStateFlow.first()

    private fun movie(
        id: Int,
        title: String = "Movie $id"
    ) = Movie(
        id = id,
        title = title,
        overview = "Overview of $title",
        releaseDate = "2024-01-01",
        poster = "https://example.com/poster$id.jpg",
        backdrop = "https://example.com/backdrop$id.jpg",
        originalTitle = "Original $title",
        originalLanguage = "en",
        popularity = 75.5,
        voteAverage = 8.5
    )

    @Test
    fun `initial state is not loading and movie is null`() = runTest {
        assertFalse(state().isLoading)
        assertNull(state().movie)
    }

    @Test
    fun `getMovieDetail loads and populates movie`() = runTest {
        val testMovie = movie(1, "Test Movie")
        coEvery { useCase(1) } returns testMovie

        viewModel.getMovieDetail(1)
        advanceUntilIdle()

        val s = state()
        assertFalse(s.isLoading)
        assertEquals(testMovie, s.movie)
    }

    @Test
    fun `getMovieDetail handles null result`() = runTest {
        coEvery { useCase(999) } returns null

        viewModel.getMovieDetail(999)
        advanceUntilIdle()

        val s = state()
        assertFalse(s.isLoading)
        assertNull(s.movie)
    }

    @Test
    fun `isLoading is true while suspended`() = runTest {
        val gate = CompletableDeferred<Movie?>()

        coEvery { useCase(1) } coAnswers {
            gate.await()
        }

        viewModel.getMovieDetail(1)
        runCurrent()

        assertTrue(state().isLoading)

        gate.complete(movie(1))
        advanceUntilIdle()

        assertFalse(state().isLoading)
    }

    @Test
    fun `retry updates movie state`() = runTest {
        coEvery { useCase(1) } returnsMany listOf(
            null,
            movie(1, "First Retry")
        )

        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        assertNull(state().movie)

        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        assertNotNull(state().movie)
        assertEquals("First Retry", state().movie!!.title)

        coVerify(exactly = 2) { useCase(1) }
    }

    @Test
    fun `different movie ids are handled correctly`() = runTest {
        val movie1 = movie(1, "Movie One")
        val movie2 = movie(2, "Movie Two")

        coEvery { useCase(1) } returns movie1
        coEvery { useCase(2) } returns movie2

        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        assertEquals("Movie One", state().movie!!.title)

        viewModel.getMovieDetail(2)
        advanceUntilIdle()
        assertEquals("Movie Two", state().movie!!.title)

        coVerify(exactly = 1) { useCase(1) }
        coVerify(exactly = 1) { useCase(2) }
    }

    @Test
    fun `movie details are correctly exposed`() = runTest {
        val testMovie = movie(1)
        coEvery { useCase(1) } returns testMovie

        viewModel.getMovieDetail(1)
        advanceUntilIdle()

        val s = state()
        assertEquals(1, s.movie!!.id)
        assertEquals("Movie 1", s.movie!!.title)
        assertEquals("Overview of Movie 1", s.movie!!.overview)
        assertEquals("2024-01-01", s.movie!!.releaseDate)
        assertEquals(75.5, s.movie!!.popularity)
        assertEquals(8.5, s.movie!!.voteAverage)
    }

    @Test
    fun `multiple calls to getMovieDetail with different ids`() = runTest {
        val movie1 = movie(1, "First Movie")
        val movie2 = movie(2, "Second Movie")

        coEvery { useCase(any()) } answers { call ->
            when (call.invocation.args[0]) {
                1 -> movie1
                2 -> movie2
                else -> null
            }
        }

        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        assertEquals("First Movie", state().movie!!.title)

        viewModel.getMovieDetail(2)
        advanceUntilIdle()
        assertEquals("Second Movie", state().movie!!.title)
    }
}




