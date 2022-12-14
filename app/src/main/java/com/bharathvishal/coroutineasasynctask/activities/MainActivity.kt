package com.bharathvishal.coroutineasasynctask.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.color.DynamicColors
import com.vishtekstudios.coroutineasasynctask.ui.theme.Material3AppTheme
import kotlinx.coroutines.*
import java.lang.ref.WeakReference


class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
    private lateinit var activityContext: Context
    private lateinit var coroutineJobObject: Job

    private var enabled1 = mutableStateOf(true)
    private var enabled2 = mutableStateOf(true)
    private var valueOfText = mutableStateOf(0)

    private val CoroutineLOGTAG = "CoroutineLOGTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Applies Material dynamic theming
        try {
            DynamicColors.applyToActivityIfAvailable(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        activityContext = this

        setContent {
            Material3AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainViewImplementation()
                }
            }
        }
    }

    //Method returns a Job Instance
    private fun simpleCoroutineTask(context: Context): Job {
        val contextRef: WeakReference<Context> = WeakReference(context)

        //This region below can be considered similar to pre-execute
        /*
         *
         */

        @Suppress("UNUSED_VARIABLE")
        val job = launch(Dispatchers.Default) {
            val context1 = contextRef.get()

            //The region below can be considered similar to DoinBg
            //Runs in a background thread
            try {
                for (i in 1..100) {
                    if (isActive) {
                        delay(250)
                        Log.d(CoroutineLOGTAG, "val : $i")
                        runOnUiThread {
                            valueOfText.value++
                        }
                    }
                    if (!isActive) {
                        break
                    }
                }
            } catch (e: CancellationException) {
                Log.d(CoroutineLOGTAG, "Encountered cancellation exception")
                //This region is similar to onCancelled in an Async Task
                runOnUiThread {
                    Toast.makeText(context1, "Coroutine cancelled", Toast.LENGTH_SHORT).show()

                    enabled1.value = true
                    enabled2.value = true
                    valueOfText.value = 0
                }
            } catch (e: Exception) {
                Log.d(CoroutineLOGTAG, "Encountered an exception")
                e.printStackTrace()
            }

            //UI Thread
            //Similar to post execute
            withContext(Dispatchers.Main) {
                val contextTemp = contextRef.get()
                Log.d(CoroutineLOGTAG, "Coroutine finished executing")
                Toast.makeText(contextTemp, "Coroutine finished executing", Toast.LENGTH_SHORT)
                    .show()

                enabled1.value = true
                enabled2.value = true
                valueOfText.value = 0
            }
        }
        return job
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MainViewImplementation() {
        Column {
            TopAppBarMain()
            CardViewMain()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarMain() {
        TopAppBar(
            title = { Text("Coroutine As Async Implementation") },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    @Composable
    fun CardViewMain() {
        Column {
            Spacer(modifier = Modifier.padding(top = 6.dp))
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                {
                    Text(
                        text = "" + valueOfText.value,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(9.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.displayMedium
                    )

                    Button(
                        onClick = {
                            coroutineJobObject = simpleCoroutineTask(activityContext)

                            enabled1.value = false
                            enabled2.value = true
                        },
                        enabled = enabled1.value
                    ) {
                        Text(text = "Run Coroutine")
                    }
                    Button(
                        onClick = {
                            if (coroutineJobObject?.isActive == true)
                                coroutineJobObject?.cancel(CancellationException("Coroutine Cancelled"))

                            enabled1.value = true
                            enabled2.value = true
                        },
                        enabled = enabled2.value
                    ) {
                        Text(text = "Stop Coroutine")
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        //Cancels the current coroutine scope and any running task on destroy
        cancel()
    }


    //Preview for jetpack composable view
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        Material3AppTheme {
            MainViewImplementation()
        }
    }
}

