package com.restaurant.models.requests.position

import kotlinx.serialization.Serializable

@Serializable
data class AddPositionRequest(val orderId: Long, val dishId: Long)