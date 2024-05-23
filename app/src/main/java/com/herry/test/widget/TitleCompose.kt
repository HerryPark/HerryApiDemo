package com.herry.test.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herry.test.R

@Composable
fun TitleCompose() {
    MessageCard(msg = Message(author = "Android", body = "Jetpack Compose"))
}

@Composable
// converts dp to TextUnit
fun Dp.textUnit(): TextUnit {
    return this.value.sp / LocalDensity.current.fontScale
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    // adds padding around each element
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .background(colorResource(id = R.color.surface_sheet_background))
            .padding(all = 8.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ic_assetstore_sidemenu_font_enabled),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                // sets image size to 40 dp
                .size(40.dp)
                // sets clip image to be circle
                .clip(CircleShape)
                // sets border width and color
                .border(1.5.dp, colorResource(id = R.color.accent), CircleShape)

        )

        // adds space between image and column
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = msg.author,
                color = colorResource(id = R.color.on_primary),
                fontSize = 14.sp)
            // adds space between author and body
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg.body,
                color = colorResource(id = R.color.on_primary),
                fontSize = 14.dp.textUnit())
        }
    }
}

@Preview
@Composable
fun PreviewMessageCard() {
    MessageCard(msg = Message(author = "Android", body = "Jetpack Compose"))
}
