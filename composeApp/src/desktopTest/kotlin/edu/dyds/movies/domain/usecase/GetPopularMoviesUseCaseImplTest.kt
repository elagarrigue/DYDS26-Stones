package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.domain.repository.MoviesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class GetPopularMoviesUseCaseImplTest {

    private lateinit var repository: MoviesRepository
    private lateinit var useCase: GetPopularMoviesUseCase

    @BeforeTest
    fun setUp() {
        repository = mockk<MoviesRepository>()
        useCase = GetPopularMoviesUseCaseImpl(repository)

    }
    private fun movie(id: Int, voteAverage: Double) = Movie(
        id = id,
        title = "title$id",
        overview = "overview",
        releaseDate = "2020-01-01",
        poster = "poster",
        backdrop = null,
        originalTitle = "original",
        originalLanguage = "en",
        popularity = 1.0,
        voteAverage = voteAverage
    )

    @Test
    fun `invoke should sort by voteAverage desc and map to QualifiedMovie with threshold`() = runTest {
        val list = listOf(
            movie(1, 5.0),
            movie(2, 8.0),
            movie(3, 6.0),
            movie(4, 4.5)
        )
        coEvery { repository.getAllMovies() } returns list

        val result = useCase()

        assertEquals(4, result.size)
        assertEquals(2, result[0].movie.id)
        assertEquals(3, result[1].movie.id)
        assertEquals(1, result[2].movie.id)
        assertEquals(4, result[3].movie.id)

        assertTrue(result[0].isGoodMovie) // id 2 -> 8.0
        assertTrue(result[1].isGoodMovie) // id 3 -> 6.0
        assertFalse(result[2].isGoodMovie) // id 1 -> 5.0
        assertFalse(result[3].isGoodMovie) // id 4 -> 4.5

        coVerify(exactly = 1) { repository.getAllMovies() }
    }

    @Test
    fun `invoke should return empty list when repository returns empty`() = runTest {
        coEvery { repository.getAllMovies() } returns emptyList()

        val result = useCase()

        assertEquals(0, result.size)
        coVerify(exactly = 1) { repository.getAllMovies() }
    }
}


