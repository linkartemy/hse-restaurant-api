package com.restaurant.models.requests.dish

import kotlinx.serialization.Serializable

@Serializable
data class AddDishRequest(val name: String, val quantity: Int, val price: Double, val cookMinutes: Int)