package com.abhiram.flowtune.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.abhiram.flowtube.models.AlbumItem
import com.abhiram.flowtube.models.Artist
import com.abhiram.flowtube.models.ArtistItem
import com.abhiram.flowtube.models.PlaylistItem
import com.abhiram.flowtube.models.SongItem
import com.abhiram.flowtube.models.WatchEndpoint
import com.abhiram.flowtube.utils.parseCookieString
import com.abhiram.flowtune.LocalDatabase
import com.abhiram.flowtune.LocalPlayerAwareWindowInsets
import com.abhiram.flowtune.LocalPlayerConnection
import com.abhiram.flowtune.R
import com.abhiram.flowtune.constants.GridThumbnailHeight
import com.abhiram.flowtune.constants.flowtubeCookieKey
import com.abhiram.flowtune.constants.ListItemHeight
import com.abhiram.flowtune.extensions.togglePlayPause
import com.abhiram.flowtune.models.toMediaMetadata
import com.abhiram.flowtune.playback.queues.YouTubeAlbumRadio
import com.abhiram.flowtune.playback.queues.YouTubeQueue
import com.abhiram.flowtune.ui.component.AlbumSmallGridItem
import com.abhiram.flowtune.ui.component.ArtistSmallGridItem
import com.abhiram.flowtune.ui.component.HideOnScrollFAB
import com.abhiram.flowtune.ui.component.LocalMenuState
import com.abhiram.flowtune.ui.component.NavigationTile
import com.abhiram.flowtune.ui.component.NavigationTitle
import com.abhiram.flowtune.ui.component.SongListItem
import com.abhiram.flowtune.ui.component.SongSmallGridItem
import com.abhiram.flowtune.ui.component.YouTubeGridItem
import com.abhiram.flowtune.ui.component.YouTubeSmallGridItem
import com.abhiram.flowtune.ui.menu.ArtistMenu
import com.abhiram.flowtune.ui.menu.SongMenu
import com.abhiram.flowtune.ui.menu.YouTubeAlbumMenu
import com.abhiram.flowtune.ui.menu.YouTubeArtistMenu
import com.abhiram.flowtune.ui.menu.YouTubePlaylistMenu
import com.abhiram.flowtune.ui.menu.YouTubeSongMenu
import com.abhiram.flowtune.ui.utils.SnapLayoutInfoProvider
import com.abhiram.flowtune.utils.rememberPreference
import com.abhiram.flowtune.viewmodels.HomeViewModel
import kotlin.random.Random

@SuppressLint("UnrememberedMutableState")
@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()

    val forgottenFavorite by viewModel.forgottenFavorite.collectAsState()
    val homeFirstAlbumRecommendation by viewModel.homeFirstAlbumRecommendation.collectAsState()
    val homeSecondAlbumRecommendation by viewModel.homeSecondAlbumRecommendation.collectAsState()

    val homeFirstArtistRecommendation by viewModel.homeFirstArtistRecommendation.collectAsState()
    val homeSecondArtistRecommendation by viewModel.homeSecondArtistRecommendation.collectAsState()
    val homeThirdArtistRecommendation by viewModel.homeThirdArtistRecommendation.collectAsState()
    val home by viewModel.home.collectAsState()

    val keepListeningSongs by viewModel.keepListeningSongs.collectAsState()
    val keepListeningAlbums by viewModel.keepListeningAlbums.collectAsState()
    val keepListeningArtists by viewModel.keepListeningArtists.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()

    val homeFirstContinuation by viewModel.homeFirstContinuation.collectAsState()
    val homeSecondContinuation by viewModel.homeSecondContinuation.collectAsState()
    val homeThirdContinuation by viewModel.homeThirdContinuation.collectAsState()

    val youtubePlaylists by viewModel.youtubePlaylists.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val mostPlayedLazyGridState = rememberLazyGridState()

    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val listenAgainLazyGridState = rememberLazyGridState()

    val flowtubeCookie by rememberPreference(flowtubeCookieKey, "")
    val isLoggedIn =
        remember(flowtubeCookie) {
            "SAPISID" in parseCookieString(flowtubeCookie)
        }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            scrollState.animateScrollTo(value = 0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = viewModel::refresh,
        indicatorPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
            val snapLayoutInfoProviderQuickPicks =
                remember(mostPlayedLazyGridState) {
                    SnapLayoutInfoProvider(
                        lazyGridState = mostPlayedLazyGridState,
                    )
                }
            val snapLayoutInfoProviderForgottenFavorite =
                remember(forgottenFavoritesLazyGridState) {
                    SnapLayoutInfoProvider(
                        lazyGridState = forgottenFavoritesLazyGridState,
                    )
                }

            Column(
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                Spacer(
                    Modifier.height(
                        LocalPlayerAwareWindowInsets.current
                            .asPaddingValues()
                            .calculateTopPadding(),
                    ),
                )
                
                

                Row(
                    modifier =
                        Modifier
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                ) {
                    

                  

                    if (isLoggedIn) {
                        
                    }
                }

                quickPicks?.let { quickPicks ->
                    NavigationTitle(
                        title = stringResource(R.string.quick_picks),
                    )

                    if (quickPicks.isEmpty()) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(ListItemHeight * 4),
                        ) {
                            Text(
                                text = stringResource(R.string.quick_picks_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    } else {
                        LazyHorizontalGrid(
                            state = mostPlayedLazyGridState,
                            rows = GridCells.Fixed(4),
                            flingBehavior =
                                rememberSnapFlingBehavior(
                                    snapLayoutInfoProviderQuickPicks,
                                ),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(ListItemHeight * 4),
                        ) {
                            items(
                                items = quickPicks,
                                key = { it.id },
                            ) { originalSong ->
                                val song by database
                                    .song(originalSong.id)
                                    .collectAsState(initial = originalSong)

                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .width(horizontalLazyGridItemWidth)
                                            .combinedClickable(
                                                onClick = {
                                                    if (song!!.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue(
                                                                WatchEndpoint(videoId = song!!.id),
                                                                song!!.toMediaMetadata(),
                                                            ),
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        SongMenu(
                                                            originalSong = song!!,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            ),
                                )
                            }
                        }
                    }
                }

                if (youtubePlaylists?.isNotEmpty() == true) {
                    NavigationTitle(
                        title = stringResource(R.string.your_ytb_playlists),
                        onClick = {
                            navController.navigate("account")
                        },
                    )
                    LazyRow(
                        contentPadding =
                            WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                    ) {
                        items(
                            items = youtubePlaylists.orEmpty(),
                            key = { it.id },
                        ) { item ->
                            YouTubeGridItem(
                                item = item,
                                modifier =
                                    Modifier
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("online_playlist/${item.id}")
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubePlaylistMenu(
                                                        playlist = item,
                                                        coroutineScope = coroutineScope,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ),
                            )
                        }
                    }
                }

                if (keepListening?.isNotEmpty() == true) {
                    keepListening?.let {
                        NavigationTitle(
                            title = stringResource(R.string.keep_listening),
                        )

                        LazyHorizontalGrid(
                            state = listenAgainLazyGridState,
                            rows = GridCells.Fixed(if (keepListening!!.size > 6) 2 else 1),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(GridThumbnailHeight * if (keepListening!!.size > 6) 2.4f else 1.2f),
                        ) {
                            keepListening?.forEach {
                                when (it) {
                                    in 0..4 ->
                                        item {
                                            ArtistSmallGridItem(
                                                artist = keepListeningArtists!![it],
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {
                                                                navController.navigate("artist/${keepListeningArtists!![it].id}")
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress,
                                                                )
                                                                menuState.show {
                                                                    ArtistMenu(
                                                                        originalArtist = keepListeningArtists!![it],
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }
                                                            },
                                                        ),
                                            )
                                        }

                                    in 5..9 ->
                                        item {
                                            AlbumSmallGridItem(
                                                song = keepListeningAlbums!![it - 5],
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {
                                                                navController.navigate(
                                                                    "album/${keepListeningAlbums!![it - 5].song.albumId}",
                                                                )
                                                            },
                                                        ),
                                            )
                                        }

                                    in 10..19 ->
                                        item {
                                            SongSmallGridItem(
                                                song = keepListeningSongs!![it - 10],
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {
                                                                if (keepListeningSongs!![it - 10].id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = keepListeningSongs!![it - 10].id),
                                                                            keepListeningSongs!![it - 10].toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress,
                                                                )
                                                                menuState.show {
                                                                    SongMenu(
                                                                        originalSong = keepListeningSongs!![it - 10],
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }
                                                            },
                                                        ),
                                                isActive = keepListeningSongs!![it - 10].song.id == mediaMetadata?.id,
                                                isPlaying = isPlaying,
                                            )
                                        }
                                }
                            }
                        }
                    }
                }

                homeFirstArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                        )

                        LazyRow(
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id },
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is PlaylistItem ->
                                                                navController.navigate(
                                                                    "online_playlist/${item.id}",
                                                                )

                                                            is SongItem -> {
                                                                if (item.id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = item.id),
                                                                            item.toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            }

                                                            is AlbumItem -> navController.navigate("album/${item.id}")

                                                            else -> navController.navigate("artist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            when (item) {
                                                                is PlaylistItem ->
                                                                    YouTubePlaylistMenu(
                                                                        playlist = item,
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )

                                                                is ArtistItem -> {
                                                                    YouTubeArtistMenu(
                                                                        artist = item,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is SongItem -> {
                                                                    YouTubeSongMenu(
                                                                        song = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is AlbumItem -> {
                                                                    YouTubeAlbumMenu(
                                                                        albumItem = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                else -> {
                                                                }
                                                            }
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                forgottenFavorite?.let { forgottenFavorite ->
                    if (forgottenFavorite.isNotEmpty() && forgottenFavorite.size > 5) {
                        NavigationTitle(
                            title = stringResource(R.string.forgotten_favorites),
                        )

                        LazyHorizontalGrid(
                            state = forgottenFavoritesLazyGridState,
                            rows = GridCells.Fixed(4),
                            flingBehavior =
                                rememberSnapFlingBehavior(
                                    snapLayoutInfoProviderForgottenFavorite,
                                ),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(ListItemHeight * 4),
                        ) {
                            items(
                                items = forgottenFavorite,
                                key = { it.id },
                            ) { originalSong ->
                                val song by database
                                    .song(originalSong.id)
                                    .collectAsState(initial = originalSong)
                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .width(horizontalLazyGridItemWidth)
                                            .combinedClickable(
                                                onClick = {
                                                    if (song!!.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue(
                                                                WatchEndpoint(videoId = song!!.id),
                                                                song!!.toMediaMetadata(),
                                                            ),
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        SongMenu(
                                                            originalSong = song!!,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            ),
                                )
                            }
                        }
                    }
                }

                home?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding =
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id },
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author =
                                            Artist(name = "YouTube Music", id = null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${playlist.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = playlist,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeFirstAlbumRecommendation?.albums?.let { albums ->
                    if (albums.recommendationAlbum.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.recommendedAlbum.name,
                        )

                        LazyRow(
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.recommendationAlbum,
                                key = { it.id },
                            ) { album ->
                                if (!album.title.contains("Presenting")) {
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${album.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = album,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeFirstContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding =
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id },
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author =
                                            Artist(name = "YouTube Music", id = null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${playlist.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = playlist,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                        )

                        LazyRow(
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id },
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is PlaylistItem ->
                                                                navController.navigate(
                                                                    "online_playlist/${item.id}",
                                                                )

                                                            is SongItem -> {
                                                                if (item.id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = item.id),
                                                                            item.toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            }

                                                            is AlbumItem -> navController.navigate("album/${item.id}")

                                                            else -> navController.navigate("artist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            when (item) {
                                                                is PlaylistItem ->
                                                                    YouTubePlaylistMenu(
                                                                        playlist = item,
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )

                                                                is ArtistItem -> {
                                                                    YouTubeArtistMenu(
                                                                        artist = item,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is SongItem -> {
                                                                    YouTubeSongMenu(
                                                                        song = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is AlbumItem -> {
                                                                    YouTubeAlbumMenu(
                                                                        albumItem = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                else -> {
                                                                }
                                                            }
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding =
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id },
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author =
                                            Artist(name = "YouTube Music", id = null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${playlist.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = playlist,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondAlbumRecommendation?.albums?.let { albums ->
                    if (albums.recommendationAlbum.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.recommendedAlbum.name,
                        )

                        LazyRow(
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.recommendationAlbum,
                                key = { it.id },
                            ) { album ->
                                if (!album.title.contains("Presenting")) {
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${album.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = album,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeThirdContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding =
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id },
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author =
                                            Artist(name = "YouTube Music", id = null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${playlist.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = playlist,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeThirdArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                        )

                        LazyRow(
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id },
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is PlaylistItem ->
                                                                navController.navigate(
                                                                    "online_playlist/${item.id}",
                                                                )

                                                            is SongItem -> {
                                                                if (item.id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = item.id),
                                                                            item.toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            }

                                                            is AlbumItem -> navController.navigate("album/${item.id}")

                                                            else -> navController.navigate("artist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            when (item) {
                                                                is PlaylistItem ->
                                                                    YouTubePlaylistMenu(
                                                                        playlist = item,
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )

                                                                is ArtistItem -> {
                                                                    YouTubeArtistMenu(
                                                                        artist = item,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is SongItem -> {
                                                                    YouTubeSongMenu(
                                                                        song = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is AlbumItem -> {
                                                                    YouTubeAlbumMenu(
                                                                        albumItem = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                else -> {
                                                                }
                                                            }
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                explorePage?.newReleaseAlbums?.let { newReleaseAlbums ->
                    NavigationTitle(
                        title = stringResource(R.string.new_release_albums),
                        onClick = {
                            navController.navigate("new_release")
                        },
                    )

                    LazyRow(
                        contentPadding =
                            WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                    ) {
                        items(
                            items = newReleaseAlbums,
                            key = { it.id },
                        ) { album ->
                            YouTubeGridItem(
                                item = album,
                                isActive = mediaMetadata?.album?.id == album.id,
                                isPlaying = isPlaying,
                                coroutineScope = coroutineScope,
                                modifier =
                                    Modifier
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("album/${album.id}")
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubeAlbumMenu(
                                                        albumItem = album,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ).animateItemPlacement(),
                            )
                        }
                    }
                }
                Spacer(
                    Modifier.height(
                        LocalPlayerAwareWindowInsets.current
                            .asPaddingValues()
                            .calculateBottomPadding(),
                    ),
                )
            }

            HideOnScrollFAB(
                visible =
                    !quickPicks.isNullOrEmpty() || !forgottenFavorite.isNullOrEmpty() || explorePage?.newReleaseAlbums?.isNotEmpty() == true,
                scrollState = scrollState,
                icon = R.drawable.casino,
                onClick = {
                    if (Random.nextBoolean() && !quickPicks.isNullOrEmpty()) {
                        val song = quickPicks!!.random()
                        playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                    } else if (explorePage?.newReleaseAlbums?.isNotEmpty() == true) {
                        val album = explorePage?.newReleaseAlbums!!.random()
                        playerConnection.playQueue(YouTubeAlbumRadio(album.playlistId))
                    }
                },
            )
        }
    }
}
