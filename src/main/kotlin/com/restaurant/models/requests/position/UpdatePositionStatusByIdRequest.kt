package com.restaurant.models.requests.position

import com.restaurant.schemas.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePositionStatusByIdRequest(val id: Long, val status: OrderStatus)