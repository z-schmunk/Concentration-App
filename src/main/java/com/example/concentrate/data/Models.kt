package com.example.concentrate.data

import kotlinx.serialization.Serializable

@Serializable
data class OnuDatabase(
    val metadata: Metadata,
    val colleges: Map<String, College>
)

@Serializable
data class Metadata(
    val institution: String,
    val abbreviation: String,
    val colleges: List<String>
)

@Serializable
data class College(
    val id: String,
    val full_name: String,
    val schools: List<String> = emptyList(),
    val programs: List<Program> = emptyList()
)

@Serializable
data class Program(
    val id: String,
    val name: String,
    val school: String? = null,
    val types: List<String>,
    val area_of_interest: List<String> = emptyList(),
    val url: String? = null,
    val degree: String? = null,
    val concentrations: List<String> = emptyList(),
    val parent_major: String? = null
)
