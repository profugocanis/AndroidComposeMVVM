package com.example.composemvvm.ui.screens.first

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composemvvm.models.Product

@Composable
fun ProductView(product: Product, modifier: Modifier) {
    Box(modifier = modifier) {
        Text(text = product.name.toString(), modifier = Modifier.padding(12.dp))
    }
}