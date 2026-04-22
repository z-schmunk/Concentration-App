package com.example.concentrate.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.concentrate.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuizAiState {
    object Idle : QuizAiState()
    object Loading : QuizAiState()
    data class Success(val recommendation: String) : QuizAiState()
    data class Error(val message: String) : QuizAiState()
}

class QuizViewModel : ViewModel() {
    private val _aiState = MutableStateFlow<QuizAiState>(QuizAiState.Idle)
    val aiState: StateFlow<QuizAiState> = _aiState.asStateFlow()

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private var generativeModel: GenerativeModel? = null

    fun getAiRecommendation(
        context: Context,
        selectedCollege: College,
        selectedInterests: Set<String>
    ) {
        _aiState.value = QuizAiState.Loading

        viewModelScope.launch {
            try {
                if (generativeModel == null) {
                    generativeModel = GenerativeModel(
                        modelName = "gemini-1.5-flash",
                        apiKey = apiKey
                    )
                }

                val db = DatabaseProvider.getDatabase(context)
                val databaseSummary = db.colleges.values.joinToString("\n") { college ->
                    "College: ${college.full_name}, Programs: ${college.programs.filter { it.types.contains("Major") }.joinToString { it.name }}"
                }

                val prompt = """
                    You are an expert academic advisor at Ohio Northern University (ONU).
                    Analyze a student's interests and recommend the best-fitting majors from the provided database.
                    
                    Available ONU Majors:
                    $databaseSummary

                    The student is interested in the ${selectedCollege.full_name}.
                    They have indicated interest in the following areas: ${selectedInterests.joinToString(", ")}.
                    
                    Please provide a personalized recommendation for the top 3 majors they should consider.
                """.trimIndent()

                val response = generativeModel?.generateContent(prompt)
                val text = response?.text

                if (text != null) {
                    _aiState.value = QuizAiState.Success(text)
                } else {
                    _aiState.value = QuizAiState.Error("Could not generate a recommendation.")
                }
            } catch (e: Exception) {
                _aiState.value = QuizAiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun reset() {
        _aiState.value = QuizAiState.Idle
    }
}
