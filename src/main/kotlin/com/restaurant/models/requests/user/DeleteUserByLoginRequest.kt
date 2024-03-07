package com.restaurant.models.requests.user

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserByLoginRequest(val login: String)