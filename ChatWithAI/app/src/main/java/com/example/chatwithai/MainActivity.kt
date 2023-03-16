package com.example.chatwithai

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chatwithai.ui.theme.ChatWithAITheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatWithAITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                MainScreen()

                }
            }
        }
    }
}




 fun updateData(
     message: String,
     ctx: Context,
     allMessages: SnapshotStateList<Message>
) {
    // on below line we are creating a variable for url.
    val url = "https://api.openai.com/v1/completions"
    val api_key = "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"
    // creating a new variable for our request queue
    val queue = Volley.newRequestQueue(ctx)
    val jsonObject: JSONObject?= JSONObject()
     jsonObject?.put("model","text-davinci-003")
     jsonObject?.put("prompt",message)
     jsonObject?.put("temperature",0)
     jsonObject?.put("max_tokens",100)
     jsonObject?.put("top_p",1)
     jsonObject?.put("frequency_penalty",0.0)
     jsonObject?.put("presence_penalty",0.0)



     // making a string request to update our data and
    // passing method as PUT. to update our data.
    val postRequest: JsonObjectRequest =
        object : JsonObjectRequest(Method.POST, url, jsonObject, Response.Listener {response->
           val responseMsg:String = response.getJSONArray("choices").getJSONObject(0).getString("text")

            val trimmedStr = responseMsg.trimStart().trim()

            allMessages.add(Message(trimmedStr,"other"))

        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError?) {
                // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(ctx, "No Connection..", Toast.LENGTH_SHORT)
                    .show()
            }
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val params:MutableMap<String,String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer $api_key"
            return params
            }

        }

     postRequest.setRetryPolicy(object : RetryPolicy{
         override fun getCurrentTimeout(): Int {
             return 50000
         }

         override fun getCurrentRetryCount(): Int {
             return 50000
         }

         override fun retry(error: VolleyError?) {

         }
     })

     queue.add(postRequest)
    // below line is to make
    // a json object request.

}

@Composable
fun MainScreen() {

    val context = LocalContext.current
    val messages = remember {
        mutableStateListOf(
            Message(text = "Hi!", senderId = "other"),
            Message("I am an AI model trained by OpenAI.","other"),
            Message("I can communicate with you in english.","other")
        )
    }


    val connectivityManager = LocalContext.current.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline = remember { mutableStateOf(false) }

    DisposableEffect(connectivityManager) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline.value = true
            }

            override fun onLost(network: Network) {
                isOnline.value = false
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        //TopBarSection
        TopBarSection(userName = "AI", profile = painterResource(id = R.drawable.ai2), isOnline = isOnline)
        //char Section
        ChatSection(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),messages = messages)
        //Message Section
        MessageSection(messages)

    }
}

@Composable
fun ChatSection(modifier: Modifier,
messages: SnapshotStateList<Message>) {

    val reversedMessages = messages.asReversed()

    LazyColumn(
        modifier = modifier,
        reverseLayout = true
    ) {

        items(reversedMessages) { message ->
            MessageItem(
                message.text,
                message.senderId
            )
            Spacer(modifier = Modifier.height(8.dp))
        }




    }


}

@Composable
fun MessageItem(text: String, senderId: String) {
    val IncomingMessage = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomEnd = 8.dp,
        bottomStart = 0.dp)

    val OutgoingMessage = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomEnd = 0.dp,
        bottomStart = 8.dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (senderId == "Self") Alignment.End else Alignment.Start

    ) {
        if (text != null) {
            if (text != "") {
                Box(
                    modifier = Modifier
                        .background(
                            if (senderId == "Self") MaterialTheme.colors.primary
                            else colorResource(id = R.color.aiColor),
                            shape = if (senderId == "Self") OutgoingMessage else IncomingMessage
                        )
                        .padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    Text(text = text, color =
                    if(senderId == "Self")Color.White
                    else Color.Black
                    )

                }


            }


        }


    }


}


@Composable
fun MessageSection(messages: SnapshotStateList<Message>) {

    val context = LocalContext.current

    val newMessage = remember {
        mutableStateOf("")
    }

    val focus = LocalFocusManager.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        elevation = 10.dp
    )
    {
        OutlinedTextField(
            placeholder = { Text(text = "Message...") },
            value =newMessage.value,
            onValueChange ={
                newMessage.value=it
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus()  }),
            shape = RoundedCornerShape(25.dp),
            trailingIcon = {
                Icon(painter = painterResource(id = R.drawable.send_resim),
                    contentDescription = "",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.clickable {
                    //tıklanınca ne olcak??
                    if(newMessage.value!="") {
                        updateData(newMessage.value,context,messages)
                        messages.add(Message(newMessage.value, "Self"))
                        newMessage.value = ""
                    }

                })

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )


    }
}

@Composable
fun TopBarSection(
    userName: String,
    profile: Painter,
    isOnline: MutableState<Boolean>
) {
    TopAppBar(modifier = Modifier
        .height(60.dp),
        backgroundColor = colorResource(id = R.color.aiColor),
        elevation = 4.dp
        ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = profile,
                contentDescription = "",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(text = userName, fontWeight = FontWeight.SemiBold)
                Text(
                    text =
                    if (isOnline.value) "Online"
                    else "Offline", fontSize = 12.sp
                )

            }

        }


    }
}



@Composable
fun ChatMessage(message: Message) {
    // Gönderenin kimliğine göre mesajın arka plan rengini belirliyoruz
    val backgroundColor = if (message.senderId == "self") {
        MaterialTheme.colors.primaryVariant
    } else {
        MaterialTheme.colors.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (message.senderId == "self") {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Box(
            modifier = Modifier
                .background(color = backgroundColor)
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Text(
                text = message.text,
                color = if (message.senderId == "self") {
                    MaterialTheme.colors.onPrimary
                } else {
                    MaterialTheme.colors.onSurface
                },
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
