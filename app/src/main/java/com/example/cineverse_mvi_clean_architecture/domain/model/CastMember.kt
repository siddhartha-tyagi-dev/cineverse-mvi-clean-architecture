package com.example.cineverse_mvi_clean_architecture.domain.model

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
)
