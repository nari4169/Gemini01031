package com.billcorea.gemini01031

import android.app.Application
import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.nl.translate.Translator

class DataViewModels(application: Application) : AndroidViewModel(application) {
    
    var transString =  mutableListOf("")
    
    fun doTranslate(outputText: String) {
        
        transString.clear()
        englishGermanTranslator.translate(outputText)
            .addOnCompleteListener { result ->
                transString.add(result.result)
                Log.e("", "result = ${result.result} \nsize=${transString.size}")
            }
            .addOnFailureListener { exception ->
                Log.e("", "addOnFailureListener ${exception.localizedMessage}")
            }
            .addOnCanceledListener {
                Log.e("", "addOnCanceledListener")
            }

    }
    
    lateinit var englishGermanTranslator : Translator
}