package com.restaurant.models.requests.position

import kotlinx.serialization.Serializable

@Serializable
data class PayPositionRequest(val positionId: Long, val paymentAmount: Double)