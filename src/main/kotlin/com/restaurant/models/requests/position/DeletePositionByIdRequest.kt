package com.restaurant.models.requests.position

import kotlinx.serialization.Serializable

@Serializable
data class DeletePositionByIdRequest(val id: Long)