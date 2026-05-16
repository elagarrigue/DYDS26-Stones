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

    val testScope = CoroutineScope(UnconfinedTestDispatcher())
    private val useCase = mockk<GetMovieDetailUseCase>(relaxed = true)

    private lateinit var viewModel: DetailViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
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
    fun `initial state should have no movie loaded`() = runTest {
        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        val initialState = emissions.last()
        assertNull(initialState.movie)
        assertFalse(initialState.isLoading)
    }

    @Test
    fun `requesting movie detail should eventually load the movie`() = runTest {
        val testMovie = movie(1, "Test Movie")
        coEvery { useCase(1) } returns testMovie

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(1)

        val finalState = emissions.last()
        assertEquals(testMovie, finalState.movie)
        assertFalse(finalState.isLoading)
    }

    @Test
    fun `requesting non-existent movie should result in no movie`() = runTest {
        coEvery { useCase(999) } returns null

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(999)

        val finalState = emissions.last()
        assertNull(finalState.movie)
        assertFalse(finalState.isLoading)
    }

    @Test
    fun `calling getMovieDetail when useCase returns null should result in no movie`() = runTest {
        coEvery { useCase(1) } returns null

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(1)
        val state = emissions.last()
        assertNull(state.movie)

        coVerify(exactly = 1) { useCase(1) }
    }

    @Test
    fun `calling getMovieDetail when useCase returns movie should result in loaded movie`() = runTest {
        coEvery { useCase(1) } returns movie(1, "First Retry")

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(1)
        val state = emissions.last()
        assertNotNull(state.movie)
        assertEquals("First Retry", state.movie!!.title)

        coVerify(exactly = 1) { useCase(1) }
    }

    @Test
    fun `given movie with id 1, when getMovieDetail(1), then state has Movie One`() = runTest {
        val movie1 = movie(1, "Movie One")
        coEvery { useCase(1) } returns movie1

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(1)
        val state1 = emissions.last()
        assertEquals("Movie One", state1.movie!!.title)

        coVerify(exactly = 1) { useCase(1) }
    }

    @Test
    fun `given movie with id 2, when getMovieDetail(2), then state has Movie Two`() = runTest {
        val movie2 = movie(2, "Movie Two")
        coEvery { useCase(2) } returns movie2

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(2)
        val state2 = emissions.last()
        assertEquals("Movie Two", state2.movie!!.title)

        coVerify(exactly = 1) { useCase(2) }
    }

    @Test
    fun `movie details are correctly exposed in final state`() = runTest {
        val testMovie = movie(1)
        coEvery { useCase(1) } returns testMovie

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(1)

        val finalState = emissions.last()
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
    fun `multiple requests with different ids should load each movie correctly - request 1`() = runTest {
        val movie1 = movie(1, "First Movie")
        val movie2 = movie(2, "Second Movie")

        coEvery { useCase(any()) } answers { call ->
            when (call.invocation.args[0]) {
                1 -> movie1
                2 -> movie2
                else -> null
            }
        }

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(1)
        assertEquals("First Movie", emissions.last().movie!!.title)
    }

    @Test
    fun `multiple requests with different ids should load each movie correctly - request 2`() = runTest {
        val movie1 = movie(1, "First Movie")
        val movie2 = movie(2, "Second Movie")

        coEvery { useCase(any()) } answers { call ->
            when (call.invocation.args[0]) {
                1 -> movie1
                2 -> movie2
                else -> null
            }
        }

        val emissions = arrayListOf<DetailViewModel.DetailUiState>()
        testScope.launch {
            viewModel.movieDetailStateFlow.collect { emissions.add(it) }
        }

        viewModel.getMovieDetail(2)
        assertEquals("Second Movie", emissions.last().movie!!.title)
    }
}
