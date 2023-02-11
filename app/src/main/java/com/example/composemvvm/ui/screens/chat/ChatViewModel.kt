package com.example.composemvvm.ui.screens.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.composemvvm.core.BaseViewModel
import com.example.composemvvm.core.Source
import com.example.composemvvm.models.Message
import com.example.composemvvm.usecases.GetMessageListUseCase
import kotlinx.coroutines.launch

class ChatViewModel(
    application: Application,
    private val getMessageListUseCase: GetMessageListUseCase
) : BaseViewModel(application) {

    var messagesState by createSourceMutableState<List<Message>>()
        private set

    init {
        load()
    }

    private fun load() {
        messagesState = Source.Processing()
        viewModelScope.launch {
            messagesState = getMessageListUseCase()
        }
    }
}