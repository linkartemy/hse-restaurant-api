package com.restaurant.models.requests.dish

import kotlinx.serialization.Serializable

@Serializable
data class DeleteDishByNameRequest(val name: String)