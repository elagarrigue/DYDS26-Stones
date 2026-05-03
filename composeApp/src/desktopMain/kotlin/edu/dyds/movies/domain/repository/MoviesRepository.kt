package edu.dyds.movies.domain.repository

import edu.dyds.movies.domain.entity.Movie

interface MoviesRepository {
    suspend fun getAllMovies(): List<Movie>
    suspend fun getMovieDetail(id: Int): Movie?
}

