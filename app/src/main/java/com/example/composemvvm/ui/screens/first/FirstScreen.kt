package com.example.composemvvm.ui.screens.first

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.composemvvm.core.BaseScreen
import com.example.composemvvm.core.Source
import com.example.composemvvm.extentions.showInfo
import com.example.composemvvm.ui.screens.second.SecondScreen
import org.koin.androidx.compose.koinViewModel

object FirstScreen : BaseScreen() {

    @Composable
    fun Screen(
        nav: NavController,
        viewModel: FirstViewModel = koinViewModel()
    ) {

        val isLoading = rememberSaveable() { mutableStateOf(false) }

        isLoading.value = viewModel.products is Source.Processing
        if (viewModel.products is Source.Error) {
            getContext().showInfo("Error")
        }

        if (isLoading.value) {
            Text(
                text = "loading...", textAlign = TextAlign.Center, modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        } else {
            Content(nav, viewModel)
        }
    }

    @Composable
    fun Content(nav: NavController, viewModel: FirstViewModel) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                val products = viewModel.products
                if (products is Source.Success) {
                    products.data?.forEach { product ->
                        item {
                            ProductView(product, modifier = Modifier
                                .fillParentMaxWidth()
                                .clickable {
                                    SecondScreen.open(nav, product)
                                })
                        }
                    }
                }
            }
        }
    }
}