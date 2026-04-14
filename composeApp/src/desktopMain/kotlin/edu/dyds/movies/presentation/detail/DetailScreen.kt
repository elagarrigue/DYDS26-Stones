@file:Suppress("FunctionName")

package edu.dyds.movies.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dydsproject.composeapp.generated.resources.Res
import dydsproject.composeapp.generated.resources.original_language
import dydsproject.composeapp.generated.resources.original_title
import dydsproject.composeapp.generated.resources.popularity
import dydsproject.composeapp.generated.resources.release_date
import dydsproject.composeapp.generated.resources.vote_average
import edu.dyds.movies.domain.entity.Movie
import edu.dyds.movies.presentation.utils.LoadingIndicator
import edu.dyds.movies.presentation.utils.NoResults
import edu.dyds.movies.presentation.viewmodel.MoviesViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(viewModel: MoviesViewModel, id: Int, onBack: () -> Unit) {

	val state by viewModel.movieDetailStateFlow.collectAsState(MoviesViewModel.MovieDetailUiState())

	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

	LaunchedEffect(Unit) {
		viewModel.getMovieDetail(id)
	}

	MaterialTheme {
		Surface {
			Scaffold(
				topBar = {
					DetailTopBar(
						title = state.movie?.title ?: "",
						onBack = onBack,
						scrollBehavior = scrollBehavior
					)
				}
			) { padding ->

				LoadingIndicator(enabled = state.isLoading, modifier = Modifier.padding(padding))

				when {
					state.movie != null -> MovieDetail(movie = state.movie!!, modifier = Modifier.padding(padding))
					state.isLoading.not() -> NoResults { viewModel.getMovieDetail(id) }
				}
			}
		}
	}
}

@Composable
private fun MovieDetail(
	movie: Movie,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.verticalScroll(rememberScrollState()),
	) {
		AsyncImage(
			model = movie.backdrop ?: movie.poster,
			contentDescription = "",
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(16f / 9f),
		)
		Text(
			text = movie.overview,
			modifier = Modifier.padding(16.dp),
		)
		Text(
			text = buildAnnotatedString {
				property(stringResource(Res.string.original_language), movie.originalLanguage)
				property(stringResource(Res.string.original_title), movie.originalTitle)
				property(stringResource(Res.string.popularity), movie.popularity.toString())
				property(stringResource(Res.string.release_date), movie.releaseDate)
				property(
					stringResource(Res.string.vote_average),
					movie.voteAverage.toString(),
					end = true
				)
			},
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.secondaryContainer)
				.padding(16.dp),
		)
	}
}

private fun AnnotatedString.Builder.property(name: String, value: String, end: Boolean = false) {
	withStyle(ParagraphStyle(lineHeight = 18.sp)) {
		withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
			append("$name: ")
		}
		append(value)
		if (!end) {
			append("\n")
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(
	title: String,
	onBack: () -> Unit,
	scrollBehavior: TopAppBarScrollBehavior
) {
	TopAppBar(
		title = { Text(text = title) },
		navigationIcon = {
			IconButton(
				onClick = onBack
			) {
				Icon(
					Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = "Back"
				)
			}
		},
		scrollBehavior = scrollBehavior
	)
}

