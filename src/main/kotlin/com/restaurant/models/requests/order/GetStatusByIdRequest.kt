package com.restaurant.models.requests.order

import kotlinx.serialization.Serializable

@Serializable
data class GetStatusByIdRequest(val id: Long)