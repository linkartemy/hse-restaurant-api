package com.restaurant.models.requests.order

import com.restaurant.schemas.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderStatusByIdRequest(val id: Long, val status: OrderStatus)