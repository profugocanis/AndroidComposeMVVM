package com.example.composemvvm.ui.screens.first

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.composemvvm.core.network.Source
import com.example.composemvvm.core.ui.BaseScreen
import com.example.composemvvm.core.ui.ScreenState
import com.example.composemvvm.logget
import com.example.composemvvm.models.Product
import com.example.composemvvm.ui.screens.second.SecondScreen
import com.example.composemvvm.ui.views.ConstraintLoadView
import com.example.composemvvm.utils.ScrollHelper
import com.google.accompanist.swiperefresh.SwipeRefresh
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

object FirstScreen : BaseScreen() {

    private class FirstScreenState: ScreenState {

        var products: MutableSet<Product>? = null
        val isLoading = mutableStateOf(false)
        val scroll = ScrollHelper()
    }

    fun open(nav: NavController) {
        navigate(nav)
    }

    @Composable
    fun Screen(
        nav: NavController, key: String, viewModel: FirstViewModel = koinViewModel(key = key)
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize().background(Color.White)) {

            val screenState = viewModel.rememberScreenState { FirstScreenState() }

            onDestroy(nav = nav) {
                logget("FirstScreen onDestroy")
            }

            val (loadView, productListView, floatingButton) = createRefs()

            HandleProducts(viewModel.productsState, viewModel, screenState)

            screenState.scroll.refreshing.isRefreshing = false

            AnimatedVisibility(visible = screenState.products != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.constrainAs(productListView) {}) {
                ProductList(nav, viewModel, screenState)
            }

            ConstraintLoadView(screenState.isLoading.value, loadView)

            AnimatedVisibility(visible = screenState.scroll.isShowFloating.value,
                enter = slideInVertically { 300 },
                exit = slideOutVertically { 300 },
                modifier = Modifier.constrainAs(floatingButton) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }) {
                val scope = rememberCoroutineScope()
                FloatingActionButton(onClick = {
                    scope.launch {
                        screenState.scroll.listState.animateScrollToItem(0)
                    }
                }) {
                    Icon(Icons.Filled.KeyboardArrowUp, "menu", tint = Color.White)
                }
            }
        }
    }

    @Composable
    private fun ProductList(
        nav: NavController, viewModel: FirstViewModel, screenState: FirstScreenState
    ) {
        SwipeRefresh(
            state = screenState.scroll.refreshing, onRefresh = {
                screenState.scroll.refreshing.isRefreshing = true
                viewModel.load(false)
            }, modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = screenState.scroll.listState,
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(screenState.products?.toList() ?: listOf(), key = { it.id.toString() }) {
                    screenState.scroll.updateScroll()
                    ProductView(it,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(PaddingValues(vertical = 8.dp, horizontal = 8.dp))
                            .clickable {
                                SecondScreen.open(nav, it)
                            })
                }

                item {
                    if (!screenState.scroll.isLastPage.value) {
                        onResume {
                            viewModel.loadMore()
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp, modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HandleProducts(
        source: Source<List<Product>>, viewModel: FirstViewModel, screenState: FirstScreenState
    ) {
        when (source) {
            is Source.Processing -> screenState.isLoading.value = true
            is Source.Success -> {
                if (viewModel.page == 0) {
                    screenState.products = mutableSetOf()
                }
                screenState.products?.addAll(source.data ?: listOf())
                screenState.isLoading.value = false
                screenState.scroll.isLastPage.value = viewModel.page >= 2
            }
            is Source.Error -> {
                ShowError(source.exception)
                screenState.isLoading.value = false
            }
        }
    }
}