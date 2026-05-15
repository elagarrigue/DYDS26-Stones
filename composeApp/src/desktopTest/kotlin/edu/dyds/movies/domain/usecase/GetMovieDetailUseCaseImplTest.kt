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
import kotlin.test.assertNull

class GetMovieDetailUseCaseImplTest {

    private lateinit var  repository: MoviesRepository
    private lateinit var useCase: GetMovieDetailUseCase
    @BeforeTest
    fun setUp() {
        repository = mockk<MoviesRepository>()
        useCase = GetMovieDetailUseCaseImpl(repository)
    }

    private fun movie(id: Int, voteAverage: Double = 5.0) = Movie(
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
    fun `invoke should return movie from repository`() = runTest {
        val expected = movie(1, 7.0)
        coEvery { repository.getMovieDetail(1) } returns expected


        val result = useCase(1)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getMovieDetail(1) }
    }

    @Test
    fun `invoke should return null when repository returns null`() = runTest {
        coEvery { repository.getMovieDetail(99) } returns null


        val result = useCase(99)

        assertNull(result)
        coVerify(exactly = 1) { repository.getMovieDetail(99) }
    }
}


