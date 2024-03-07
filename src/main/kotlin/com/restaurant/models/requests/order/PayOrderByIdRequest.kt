package com.restaurant.models.requests.order

import kotlinx.serialization.Serializable

@Serializable
data class PayOrderByIdRequest(val id: Long, val paymentSum: Double)