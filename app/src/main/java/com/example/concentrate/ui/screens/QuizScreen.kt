package com.example.concentrate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.concentrate.data.College
import com.example.concentrate.data.DatabaseProvider
import com.example.concentrate.data.Program

enum class QuizStep {
    Intro,
    CollegeSelection,
    PersonalityQuestions,
    MajorResults,
    ExploreChoice,
    MinorResults,
    ConcentrationResults
}

enum class ExplorationChoice {
    Minor,
    Concentration,
    Done
}

data class PersonalityQuestion(
    val text: String,
    val relatedInterests: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen() {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    
    var currentStep by remember { mutableStateOf(QuizStep.Intro) }
    var selectedCollege by remember { mutableStateOf<College?>(null) }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedMajor by remember { mutableStateOf<Program?>(null) }

    val personalityQuestions = remember {
        listOf(
            PersonalityQuestion("Do you enjoy working with numbers, patterns, or data analysis?", listOf("Math", "Accounting and Business")),
            PersonalityQuestion("Are you passionate about helping people through healthcare or wellness?", listOf("Health and Medical")),
            PersonalityQuestion("Do you like to design, build, or solve complex technical problems?", listOf("Engineering and Technology")),
            PersonalityQuestion("Are you interested in creative expression, music, or the arts?", listOf("Arts & Humanities")),
            PersonalityQuestion("Do you find yourself interested in law, government, or social justice issues?", listOf("Justice and Legal", "Social Sciences")),
            PersonalityQuestion("Do you enjoy teaching others or taking on leadership roles?", listOf("Teaching, Education and Leadership")),
            PersonalityQuestion("Are you curious about the natural world, biology, or chemistry?", listOf("Natural and Life Sciences")),
            PersonalityQuestion("Do you enjoy writing, storytelling, or media production?", listOf("English, Communications and Writing")),
            PersonalityQuestion("Do you enjoy learning about different cultures, languages, or history?", listOf("Arts & Humanities", "Social Sciences")),
            PersonalityQuestion("Are you interested in how businesses operate, marketing, or entrepreneurship?", listOf("Accounting and Business")),
            PersonalityQuestion("Do you like working with your hands to create or repair things?", listOf("Engineering and Technology")),
            PersonalityQuestion("Are you interested in psychology, human behavior, or social work?", listOf("Social Sciences")),
            PersonalityQuestion("Do you enjoy being outdoors and learning about the environment or agriculture?", listOf("Natural and Life Sciences")),
            PersonalityQuestion("Are you interested in how technology impacts society and ethics?", listOf("Social Sciences", "Engineering and Technology")),
            PersonalityQuestion("Do you enjoy analyzing economic trends or global markets?", listOf("Accounting and Business")),
            PersonalityQuestion("Are you interested in medical research or laboratory work?", listOf("Natural and Life Sciences", "Health and Medical"))
        )
    }

    val onBack: () -> Unit = {
        when (currentStep) {
            QuizStep.Intro -> {}
            QuizStep.CollegeSelection -> { currentStep = QuizStep.Intro }
            QuizStep.PersonalityQuestions -> {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex -= 1
                } else {
                    currentStep = QuizStep.CollegeSelection
                }
            }
            QuizStep.MajorResults -> { currentStep = QuizStep.PersonalityQuestions }
            QuizStep.ExploreChoice -> { currentStep = QuizStep.MajorResults }
            QuizStep.MinorResults -> { currentStep = QuizStep.ExploreChoice }
            QuizStep.ConcentrationResults -> { currentStep = QuizStep.ExploreChoice }
        }
    }

    Scaffold(
        topBar = {
            if (currentStep != QuizStep.Intro) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            when (currentStep) {
                                QuizStep.MajorResults -> "Select Your Major"
                                QuizStep.ExploreChoice -> "Explore Further"
                                QuizStep.MinorResults -> "Recommended Minors"
                                QuizStep.ConcentrationResults -> "Concentrations"
                                else -> "Major Finder Quiz"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "QuizStepAnimation"
            ) { step ->
                when (step) {
                    QuizStep.Intro -> QuizIntro { currentStep = QuizStep.CollegeSelection }
                    QuizStep.CollegeSelection -> CollegeSelection(database.colleges.values.toList()) {
                        selectedCollege = it
                        currentStep = QuizStep.PersonalityQuestions
                        currentQuestionIndex = 0
                        selectedInterests = emptySet()
                    }
                    QuizStep.PersonalityQuestions -> {
                        PersonalityQuestionView(
                            question = personalityQuestions[currentQuestionIndex],
                            onAnswer = { interested ->
                                if (interested) {
                                    selectedInterests = selectedInterests + personalityQuestions[currentQuestionIndex].relatedInterests
                                }
                                if (currentQuestionIndex < personalityQuestions.size - 1) {
                                    currentQuestionIndex += 1
                                } else {
                                    currentStep = QuizStep.MajorResults
                                }
                            }
                        )
                    }
                    QuizStep.MajorResults -> MajorResultsView(
                        college = selectedCollege!!,
                        interests = selectedInterests,
                        onMajorSelected = {
                            selectedMajor = it
                            currentStep = QuizStep.ExploreChoice
                        }
                    )
                    QuizStep.ExploreChoice -> ExploreChoiceView(
                        major = selectedMajor!!,
                        onChoice = { choice ->
                            when (choice) {
                                ExplorationChoice.Minor -> currentStep = QuizStep.MinorResults
                                ExplorationChoice.Concentration -> currentStep = QuizStep.ConcentrationResults
                                ExplorationChoice.Done -> currentStep = QuizStep.Intro
                            }
                        }
                    )
                    QuizStep.MinorResults -> MinorResultsView(
                        college = selectedCollege!!,
                        interests = selectedInterests
                    )
                    QuizStep.ConcentrationResults -> ConcentrationResultsView(
                        major = selectedMajor!!,
                        college = selectedCollege!!
                    )
                }
            }
        }
    }
}

@Composable
fun QuizIntro(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Find Your Path at ONU",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Take this quick quiz to discover which college and major fits your personality and goals.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Start Quiz", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CollegeSelection(colleges: List<College>, onSelected: (College) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Step 1: Which college sounds most interesting?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Choose the general area you'd like to explore.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(colleges) { college ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelected(college) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = college.full_name,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalityQuestionView(question: PersonalityQuestion, onAnswer: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onAnswer(true) },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Yes!")
            }
            OutlinedButton(
                onClick = { onAnswer(false) },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Not really")
            }
        }
    }
}

@Composable
fun MajorResultsView(
    college: College,
    interests: Set<String>,
    onMajorSelected: (Program) -> Unit
) {
    val matchingMajors = college.programs.filter { program ->
        program.types.contains("Major") && (interests.isEmpty() || program.area_of_interest.any { it in interests })
    }.sortedBy { it.name }

    val majorsToShow = if (matchingMajors.isEmpty()) {
        college.programs.filter { it.types.contains("Major") }.sortedBy { it.name }
    } else {
        matchingMajors
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Select a Major",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (matchingMajors.isEmpty()) 
                "We didn't find a perfect match for your specific interests, but here are all majors in ${college.full_name}:"
            else 
                "Based on your interests, here are some recommended majors in ${college.full_name}:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        ProgramsList(
            programs = majorsToShow,
            onProgramSelected = onMajorSelected
        )
    }
}

@Composable
fun ExploreChoiceView(major: Program, onChoice: (ExplorationChoice) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Great choice: ${major.name}!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Would you like to explore related minors or specific concentrations for this major?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { onChoice(ExplorationChoice.Minor) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Explore Minors")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { onChoice(ExplorationChoice.Concentration) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Explore Concentrations")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = { onChoice(ExplorationChoice.Done) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("No, I'm done")
        }
    }
}

@Composable
fun MinorResultsView(
    college: College,
    interests: Set<String>
) {
    val matchingMinors = college.programs.filter { program ->
        program.types.contains("Minor") && (interests.isEmpty() || program.area_of_interest.any { it in interests })
    }.sortedBy { it.name }

    val minorsToShow = if (matchingMinors.isEmpty()) {
        college.programs.filter { it.types.contains("Minor") }.sortedBy { it.name }
    } else {
        matchingMinors
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Recommended Minors",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Here are some minors that match your interests:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        ProgramsList(
            programs = minorsToShow
        )
    }
}

@Composable
fun ConcentrationResultsView(
    major: Program,
    college: College
) {
    val concentrations = college.programs.filter { program ->
        program.types.contains("Concentration") && 
        (program.parent_major == major.name || major.concentrations.contains(program.name))
    }.toMutableList()

    major.concentrations.forEach { name ->
        if (concentrations.none { it.name == name }) {
            concentrations.add(Program(id = name, name = name, types = listOf("Concentration")))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Concentrations for ${major.name}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (concentrations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No specific concentrations found for this major.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            ProgramsList(
                programs = concentrations.sortedBy { it.name }
            )
        }
    }
}

@Composable
fun ProgramsList(
    programs: List<Program>,
    onProgramSelected: ((Program) -> Unit)? = null
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(programs) { program ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (onProgramSelected != null) it.clickable { onProgramSelected(program) } else it },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = program.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (program.degree != null) {
                            Text(
                                text = program.degree,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    if (program.area_of_interest.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = program.area_of_interest.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}