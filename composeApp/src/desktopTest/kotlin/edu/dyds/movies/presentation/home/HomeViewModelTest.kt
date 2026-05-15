package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.entity.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {


    val testScope = CoroutineScope(UnconfinedTestDispatcher())
    private val useCase = mockk<GetPopularMoviesUseCase>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = HomeViewModel(useCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }


    private fun movie(
        id: Int,
        isGood: Boolean
    ) = QualifiedMovie(
        movie = Movie(
            id = id,
            title = "Movie $id",
            overview = "",
            releaseDate = "",
            poster = "",
            backdrop = null,
            originalTitle = "",
            originalLanguage = "en",
            popularity = 0.0,
            voteAverage = 0.0
        ),
        isGoodMovie = isGood
    )

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        val s = emissions.last()
        assertFalse(s.isLoading)
        assertTrue(s.movies.isEmpty())

        job.cancel()
    }

    @Test
    fun `getAllMovies loads and populates movies`() = runTest {
        val movies = listOf(movie(1, true), movie(2, false))
        coEvery { useCase.invoke() } returns movies

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        val s = emissions.last()
        assertFalse(s.isLoading)
        assertEquals(movies, s.movies)

        job.cancel()
    }

    @Test
    fun `getAllMovies handles empty result`() = runTest {
        coEvery { useCase.invoke() } returns emptyList()

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        val s = emissions.last()
        assertFalse(s.isLoading)
        assertTrue(s.movies.isEmpty())

        job.cancel()
    }

    @Test
    fun `isLoading is true while suspended`() = runTest {
        coEvery { useCase.invoke() } coAnswers {
            delay(10)
            listOf(movie(1, true))
        }

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        assertTrue(emissions.any { it.isLoading })

        advanceUntilIdle()

        val final = emissions.last()
        assertFalse(final.isLoading)

        job.cancel()
    }

    @Test
    fun `getAllMovies updates state to empty when use case returns empty`() = runTest {
        coEvery { useCase.invoke() } returns emptyList()

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        assertTrue(emissions.last().movies.isEmpty())
        coVerify(exactly = 1) { useCase.invoke() }

        job.cancel()
    }

    @Test
    fun `getAllMovies updates state with new data on second call`() = runTest {
        coEvery { useCase.invoke() } returnsMany listOf(
            emptyList(),
            listOf(movie(1, true))
        )

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        viewModel.getAllMovies()

        assertEquals(1, emissions.last().movies.size)
        coVerify(exactly = 2) { useCase.invoke() }

        job.cancel()
    }

    @Test
    fun `good and bad movies are correctly exposed`() = runTest {
        val movies = listOf(
            movie(1, true),
            movie(2, false)
        )

        coEvery { useCase.invoke() } returns movies

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        val s = emissions.last()
        assertEquals(1, s.movies.count { it.isGoodMovie })
        assertEquals(1, s.movies.count { !it.isGoodMovie })

        job.cancel()
    }

    @Test
    fun `only good movies`() = runTest {
        coEvery { useCase.invoke() } returns listOf(movie(1, true))

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        assertTrue(emissions.last().movies.all { it.isGoodMovie })

        job.cancel()
    }

    @Test
    fun `only bad movies`() = runTest {
        coEvery { useCase.invoke() } returns listOf(movie(1, false))

        val emissions = arrayListOf<HomeViewModel.HomeUiState>()
        val job = testScope.launch {
            viewModel.homeStateFlow.collect { emissions.add(it) }
        }

        viewModel.getAllMovies()

        assertTrue(emissions.last().movies.none { it.isGoodMovie })

        job.cancel()
    }

}