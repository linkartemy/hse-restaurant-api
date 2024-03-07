package com.restaurant.models.requests.stats

import kotlinx.serialization.Serializable

@Serializable
data class GetMostPopularDishesRequest(val count: Int)
