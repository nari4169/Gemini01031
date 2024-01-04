package com.billcorea.gemini01031

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.billcorea.gemini01031.summarize.SummarizeUiState
import com.billcorea.gemini01031.summarize.SummarizeViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.billcorea.gemini01031.ui.theme.Gemini01031Theme
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : ComponentActivity() {
    
    private val dataViewModels : DataViewModels by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create an English-Korean translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
        dataViewModels.englishGermanTranslator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        dataViewModels.englishGermanTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                toast("Translate Package Download Completed")
            }
            .addOnFailureListener { exception ->
                toast("Translate Package Download late ... [${exception.localizedMessage}]")
            }
        
        setContent {
            Gemini01031Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro",
                        apiKey = BuildConfig.apiKey
                    )
                    val viewModel = SummarizeViewModel(generativeModel)
                    SummarizeRoute(dataViewModels, viewModel)
                }
            }
        }
    }
    
    private fun Activity.toast(msg : String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
internal fun SummarizeRoute(
    dataViewModels: DataViewModels,
    summarizeViewModel: SummarizeViewModel = viewModel()
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()
    
    SummarizeScreen(dataViewModels, summarizeUiState, onSummarizeClicked = { inputText ->
        summarizeViewModel.summarize(inputText, dataViewModels.englishGermanTranslator)
    })
}

@Composable
fun SummarizeScreen(
    dataViewModels: DataViewModels,
    uiState: SummarizeUiState = SummarizeUiState.Initial,
    onSummarizeClicked: (String) -> Unit = {}
) {
    var prompt by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            Row {
                TextField(
                    value = prompt,
                    label = { Text(stringResource(R.string.summarize_label)) },
                    placeholder = { Text(stringResource(R.string.summarize_hint)) },
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .weight(8f)
                )
                TextButton(
                    onClick = {
                        if (prompt.isNotBlank()) {
                            onSummarizeClicked(prompt)
                        }
                    },
                    
                    modifier = Modifier
                        .weight(2f)
                        .padding(all = 4.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(stringResource(R.string.action_go))
                }
            }
        }
        
        item {
            when (uiState) {
                SummarizeUiState.Initial -> {
                    // Nothing is shown
                }
                
                SummarizeUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(all = 8.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SummarizeUiState.Success -> {
                    Row(modifier = Modifier.padding(all = 8.dp)) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "Person Icon"
                        )
                        Text(
                            text = uiState.outputText,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    
                }
                
                is SummarizeUiState.Error -> {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(all = 8.dp)
                    )
                }
            }
        }
        
//        itemsIndexed(dataViewModels.transString) { index, item ->
//            Box(modifier = Modifier.fillMaxWidth()) {
//                Text(
//                    text = item,
//                    modifier = Modifier.padding(horizontal = 8.dp)
//                )
//            }
//        }
    }
}
