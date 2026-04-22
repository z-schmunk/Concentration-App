package com.example.concentrate.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentrate.BuildConfig
import com.example.concentrate.ui.screens.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf(Message("Hi! I'm your ONU Major Assistant. I have information about all our colleges and programs. How can I help you today?", false))
    
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private var generativeModel: GenerativeModel? = null

    fun sendMessage(userText: String, context: Context) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            messages.add(Message("Error: API Key not found.", false))
            return
        }

        messages.add(Message(userText, true))
        
        viewModelScope.launch {
            try {
                val responseText = withContext(Dispatchers.IO) {
                    if (generativeModel == null) {
                        generativeModel = GenerativeModel(
                            modelName = "gemini-1.5-flash",
                            apiKey = apiKey
                        )
                    }

                    val db = DatabaseProvider.getDatabase(context)
                    val databaseSummary = db.colleges.values.joinToString("\n") { college -> 
                        "College: ${college.full_name}, Programs: ${college.programs.joinToString { it.name }}" 
                    }

                    val fullPrompt = """
                        You are a helpful advisor for Ohio Northern University. 
                        Database of programs:
                        $databaseSummary
                        
                        User Question: $userText
                    """.trimIndent()

                    val response = generativeModel?.generateContent(fullPrompt)
                    response?.text
                }

                responseText?.let {
                    messages.add(Message(it, false))
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "GEMINI ERROR", e)
                messages.add(Message("Error: ${e.localizedMessage}", false))
            }
        }
    }
}
