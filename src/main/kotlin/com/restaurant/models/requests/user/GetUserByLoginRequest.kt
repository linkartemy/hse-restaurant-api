package com.restaurant.models.requests.user

import kotlinx.serialization.Serializable

@Serializable
data class GetUserByLoginRequest(val login: String)