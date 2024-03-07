package com.restaurant.models.requests.stats

import kotlinx.serialization.Serializable

@Serializable
data class GetOrdersCountByPeriodRequest(val startDate: String, val endDate: String)