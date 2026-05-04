package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entity.Movie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotSame
import kotlin.test.assertFailsWith

class MoviesLocalDataSourceImplTest {

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
    fun `getPopularMovies should return saved list and be a copy`() {
        val local = MoviesLocalDataSourceImpl()

        // initially empty
        assertEquals(0, local.getPopularMovies().size)

        val movies = listOf(movie(1), movie(2))
        local.savePopularMovies(movies)

        val retrieved = local.getPopularMovies()
        assertEquals(2, retrieved.size)
        // should not be same instance as the original list (copy)
        assertNotSame(movies, retrieved)
    }

    @Test
    fun `getMovieDetail should return null when not present and after saving should return movie`() {
        val local = MoviesLocalDataSourceImpl()

        assertNull(local.getMovieDetail(1))

        val m = movie(1)
        local.savePopularMovies(listOf(m))

        val retrieved = local.getMovieDetail(1)
        assertEquals(m, retrieved)
    }

    @Test
    fun `saveMovieDetail adds movie at index and getMovieDetail returns first matching`() {
        val local = MoviesLocalDataSourceImpl()

        val m1 = movie(1)
        val m2 = movie(2)
        local.savePopularMovies(listOf(m1, m2))

        // create an updated movie with same id as m1 and save at index 0
        val updated = movie(1, voteAverage = 9.0)
        local.saveMovieDetail(updated, 0)

        // getMovieDetail returns first matching id -> should be the newly inserted one
        val retrieved = local.getMovieDetail(1)
        assertEquals(updated, retrieved)
    }

    @Test
    fun `saveMovieDetail with index greater than size should throw IndexOutOfBounds`() {
        val local = MoviesLocalDataSourceImpl()

        val m1 = movie(1)
        local.savePopularMovies(listOf(m1))

        val newMovie = movie(2)

        assertFailsWith<IndexOutOfBoundsException> {
            local.saveMovieDetail(newMovie, 5)
        }
    }
}


