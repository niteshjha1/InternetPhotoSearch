package com.niteshkumarjha.internetphotosearch

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun MainActivityUI(openSearchActivity: (String) -> Unit, openMyGalleryActivity: () -> Unit) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var isMenuOpen by remember { mutableStateOf(false) }
    if (isMenuOpen) {
        MenuDialog(onDismiss = { isMenuOpen = false }, onMyGalleryClick = openMyGalleryActivity)
    }
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart
    ) {
        IconButton(
            onClick = { isMenuOpen = true }, modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.find_the_right_image_easy),
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.enter_text),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    singleLine = true,
                )

                Button(
                    onClick = {
                        if (searchText.text.isNotEmpty()) {
                            openSearchActivity(searchText.text)
                        }
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(text = stringResource(id = R.string.search))
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 300.dp)
        )

        if (isMenuOpen) {
            MenuDialog(onDismiss = { isMenuOpen = false }, onMyGalleryClick = openMyGalleryActivity)
        }
    }
}

@Composable
fun MenuDialog(onDismiss: () -> Unit, onMyGalleryClick: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            color = Color(0xAA000000),
            modifier = Modifier.fillMaxSize(),
            contentColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MenuItem(text = "My Gallery", onClick = onMyGalleryClick)
                MenuItem(text = "Login", onClick = onMyGalleryClick)
                MenuItem(text = "Learn More", onClick = onMyGalleryClick)
                MenuItem(text = "About", onClick = onMyGalleryClick)
                MenuItem(text = "Links", onClick = onMyGalleryClick)
            }
        }
    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}
