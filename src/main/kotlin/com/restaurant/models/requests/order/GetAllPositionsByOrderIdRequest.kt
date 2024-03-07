package com.restaurant.models.requests.order

import kotlinx.serialization.Serializable

@Serializable
data class GetAllPositionsByOrderIdRequest(val orderId: Long)