package com.restaurant.models.requests.order

import kotlinx.serialization.Serializable

@Serializable
data class GetOrderByIdRequest(val id: Long)