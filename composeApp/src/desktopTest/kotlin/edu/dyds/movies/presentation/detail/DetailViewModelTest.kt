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

    private val useCase = mockk<GetMovieDetailUseCase>(relaxed = true)
    private val dispatcher = StandardTestDispatcher()

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
    fun `initial state should have no movie loaded`() {
        val initialState = viewModel.movieDetailStateFlow.value
        assertNull(initialState.movie)
        assertFalse(initialState.isLoading)
    }

    @Test
    fun `requesting movie detail should eventually load the movie`() = runTest {
        val testMovie = movie(1, "Test Movie")
        coEvery { useCase(1) } returns testMovie

        viewModel.getMovieDetail(1)
        advanceUntilIdle()

        val finalState = viewModel.movieDetailStateFlow.value
        assertEquals(testMovie, finalState.movie)
        assertFalse(finalState.isLoading)
    }

    @Test
    fun `requesting non-existent movie should result in no movie`() = runTest {
        coEvery { useCase(999) } returns null

        viewModel.getMovieDetail(999)
        advanceUntilIdle()

        val finalState = viewModel.movieDetailStateFlow.value
        assertNull(finalState.movie)
        assertFalse(finalState.isLoading)
    }

    @Test
    fun `calling getMovieDetail again with new id should load the new movie`() = runTest {
        coEvery { useCase(1) } returnsMany listOf(
            null,
            movie(1, "First Retry")
        )

        // First call returns null
        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        val firstState = viewModel.movieDetailStateFlow.value
        assertNull(firstState.movie)

        // Second call returns movie
        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        val secondState = viewModel.movieDetailStateFlow.value
        assertNotNull(secondState.movie)
        assertEquals("First Retry", secondState.movie!!.title)

        coVerify(exactly = 2) { useCase(1) }
    }

    @Test
    fun `different movie ids should load their respective movies`() = runTest {
        val movie1 = movie(1, "Movie One")
        val movie2 = movie(2, "Movie Two")

        coEvery { useCase(1) } returns movie1
        coEvery { useCase(2) } returns movie2

        viewModel.getMovieDetail(1)
        advanceUntilIdle()
        val state1 = viewModel.movieDetailStateFlow.value
        assertEquals("Movie One", state1.movie!!.title)

        viewModel.getMovieDetail(2)
        advanceUntilIdle()
        val state2 = viewModel.movieDetailStateFlow.value
        assertEquals("Movie Two", state2.movie!!.title)

        coVerify(exactly = 1) { useCase(1) }
        coVerify(exactly = 1) { useCase(2) }
    }

    @Test
    fun `movie details are correctly exposed in final state`() = runTest {
        val testMovie = movie(1)
        coEvery { useCase(1) } returns testMovie

        viewModel.getMovieDetail(1)
        advanceUntilIdle()

        val finalState = viewModel.movieDetailStateFlow.value
        assertNotNull(finalState.movie)
        with(finalState.movie!!) {
            assertEquals(1, id)
            assertEquals("Movie 1", title)
            assertEquals("Overview of Movie 1", overview)
            assertEquals("2024-01-01", releaseDate)
            assertEquals(75.5, popularity)
            assertEquals(8.5, voteAverage)
        }
    }

    @Test
    fun `multiple requests with different ids should load each movie correctly`() = runTest {
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
        assertEquals("First Movie", viewModel.movieDetailStateFlow.value.movie!!.title)

        viewModel.getMovieDetail(2)
        advanceUntilIdle()
        assertEquals("Second Movie", viewModel.movieDetailStateFlow.value.movie!!.title)
    }
}




