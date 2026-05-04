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

    private val dispatcher = StandardTestDispatcher()
    private val useCase = mockk<GetPopularMoviesUseCase>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = HomeViewModel(useCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }


    private suspend fun lastState(): HomeViewModel.HomeUiState = viewModel.homeStateFlow.first()

    private fun TestScope.startCollector(expectedCount: Int): Pair<Job, MutableList<HomeViewModel.HomeUiState>> {
        val emissions = mutableListOf<HomeViewModel.HomeUiState>()
        val job = launch {
            viewModel.homeStateFlow.take(expectedCount).toCollection(emissions)
        }
        return Pair(job, emissions)
    }

    private suspend fun TestScope.collectStates(expectedCount: Int, action: suspend () -> Unit): List<HomeViewModel.HomeUiState> {
        val (job, emissions) = startCollector(expectedCount)
        action()
        advanceUntilIdle()
        job.cancel()
        return emissions
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
        val s = lastState()
        assertFalse(s.isLoading)
        assertTrue(s.movies.isEmpty())
    }

    @Test
    fun `getAllMovies loads and populates movies`() = runTest {
        val movies = listOf(movie(1, true), movie(2, false))
        coEvery { useCase.invoke() } returns movies

        val emissions = collectStates(3) { viewModel.getAllMovies() }

        val s = emissions.last()
        assertFalse(s.isLoading)
        assertEquals(movies, s.movies)
    }

    @Test
    fun `getAllMovies handles empty result`() = runTest {
        coEvery { useCase.invoke() } returns emptyList()

        val emissions = collectStates(3) { viewModel.getAllMovies() }
        val s = emissions.last()
        assertFalse(s.isLoading)
        assertTrue(s.movies.isEmpty())
    }

    @Test
    fun `isLoading is true while suspended`() = runTest {
        val gate = CompletableDeferred<Unit>()

        coEvery { useCase.invoke() } coAnswers {
            gate.await()
            listOf(movie(1, true))
        }

        val emissions = mutableListOf<HomeViewModel.HomeUiState>()
        val job = launch { viewModel.homeStateFlow.take(2).toCollection(emissions) }

        viewModel.getAllMovies()

        runCurrent()


        assertTrue(emissions.any { it.isLoading })

        gate.complete(Unit)
        advanceUntilIdle()

        val final = viewModel.homeStateFlow.first()
        assertFalse(final.isLoading)
        job.cancel()
    }

    @Test
    fun `retry updates state`() = runTest {
        coEvery { useCase.invoke() } returnsMany listOf(
            emptyList(),
            listOf(movie(1, true))
        )


        var emissions = collectStates(3) { viewModel.getAllMovies() }
        assertTrue(emissions.last().movies.isEmpty())


        emissions = collectStates(3) { viewModel.getAllMovies() }
        assertEquals(1, emissions.last().movies.size)

        coVerify(exactly = 2) { useCase.invoke() }
    }

    @Test
    fun `good and bad movies are correctly exposed`() = runTest {
        val movies = listOf(
            movie(1, true),
            movie(2, false)
        )

        coEvery { useCase.invoke() } returns movies

        val emissions = collectStates(3) { viewModel.getAllMovies() }
        val s = emissions.last()

        assertEquals(1, s.movies.count { it.isGoodMovie })
        assertEquals(1, s.movies.count { !it.isGoodMovie })
    }

    @Test
    fun `only good movies`() = runTest {
        coEvery { useCase.invoke() } returns listOf(movie(1, true))

        val emissions = collectStates(3) { viewModel.getAllMovies() }
        assertTrue(emissions.last().movies.all { it.isGoodMovie })
    }

    @Test
    fun `only bad movies`() = runTest {
        coEvery { useCase.invoke() } returns listOf(movie(1, false))

        val emissions = collectStates(3) { viewModel.getAllMovies() }
        assertTrue(emissions.last().movies.none { it.isGoodMovie })
    }

}