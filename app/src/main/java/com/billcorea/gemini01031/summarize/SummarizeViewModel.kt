package com.billcorea.gemini01031.summarize

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SummarizeViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {
    
    private val _uiState: MutableStateFlow<SummarizeUiState> =
        MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
        _uiState.asStateFlow()
    
    fun summarize(inputText: String, englishGermanTranslator: Translator) {
        _uiState.value = SummarizeUiState.Loading
        
        val prompt = "Summarize the following text for me: $inputText"
        
        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let { outputContent ->
                    englishGermanTranslator.translate(outputContent)
                        .addOnCompleteListener { result ->
                            Log.e("", "result = ${result.result}")
                            _uiState.value = SummarizeUiState.Success(outputContent + "\n" + result.result)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("", "addOnFailureListener ${exception.localizedMessage}")
                            _uiState.value = SummarizeUiState.Success(outputContent)
                        }
                        .addOnCanceledListener {
                            Log.e("", "addOnCanceledListener")
                            _uiState.value = SummarizeUiState.Success(outputContent)
                        }
                }
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}