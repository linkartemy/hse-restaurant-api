package com.restaurant.models.requests.user

import com.restaurant.schemas.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val login: String,
    val password: String,
    val role: UserRole
)