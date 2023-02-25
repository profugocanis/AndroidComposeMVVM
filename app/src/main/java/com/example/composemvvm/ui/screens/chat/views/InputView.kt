package com.example.composemvvm.ui.screens.chat.views

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.composemvvm.R
import com.example.composemvvm.extentions.CustomBlue
import com.example.composemvvm.extentions.CustomLightGray
import com.example.composemvvm.extentions.onBounceClick
import com.example.composemvvm.models.MessageData
import com.example.composemvvm.ui.activities.MainActivity
import com.example.composemvvm.ui.screens.chat.ChatScreen
import com.example.composemvvm.ui.screens.chat.ChatScreenState
import com.example.composemvvm.ui.screens.chat.ChatViewModel
import com.example.composemvvm.utils.KeyboardManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InputView(
    viewModel: ChatViewModel,
    screenState: ChatScreenState,
    modifier: Modifier
) {
    val text = remember { mutableStateOf(TextFieldValue("")) }
    Column(modifier = modifier.fillMaxWidth()) {

        Divider()

        AnimatedVisibility(
            visible = screenState.replayMessage.value != null,
            enter = expandVertically(
                spring(
                    stiffness = Spring.StiffnessLow,
                    visibilityThreshold = IntSize.VisibilityThreshold
                ),
            ),
            exit = shrinkVertically(),
        ) {
            ReplayMessage(screenState)
        }

        TextField(
            value = text.value,
            maxLines = 3,
            placeholder = { Text(text = "Enter text") },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            onValueChange = { text.value = it },
            trailingIcon = {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.onBounceClick {
                        ChatScreen.sendMessage(text.value.text.trim(), screenState, viewModel)
                        text.value = TextFieldValue("")
                    })
            },
            leadingIcon = {
                val activity = ChatScreen.getActivity() as? MainActivity
                Icon(painter = painterResource(id = R.drawable.ic_image),
                    contentDescription = null,
                    modifier = Modifier.onBounceClick {
                        KeyboardManager.hideKeyBoard(activity)
                        activity?.imageHelper?.select {
                            ChatScreen.sendImage(it, screenState, viewModel)
                        }
                    })
            },
            shape = CircleShape,
            colors = TextFieldDefaults.textFieldColors(
                leadingIconColor = Color.CustomBlue,
                trailingIconColor = Color.CustomBlue,
                backgroundColor = Color.CustomLightGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ReplayMessage(screenState: ChatScreenState) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {

        val (titleView, messageView, closeIcon) = createRefs()

        Text(
            text = "Replay",
            color = Color.Gray,
            modifier = Modifier.constrainAs(titleView) {
                start.linkTo(parent.start, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            })

        val messageModifier = Modifier.constrainAs(messageView) {
            start.linkTo(titleView.end, margin = 8.dp)
            if (screenState.replayMessage.value?.data is MessageData.Text) {
                end.linkTo(closeIcon.start)
            }
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
        }
        when (val messageData = screenState.replayMessage.value?.data) {
            is MessageData.Text -> {
                Text(
                    text = messageData.text ?: "",
                    modifier = messageModifier,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            is MessageData.Image -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(messageData.getImageData())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = messageModifier.size(44.dp)
                )
            }
            else -> {}
        }

        Icon(
            Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier
                .constrainAs(closeIcon) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .padding(8.dp)
                .onBounceClick {
                    screenState.replayMessage.value = null
                })
    }
}