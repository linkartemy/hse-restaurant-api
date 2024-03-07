package com.restaurant.models.requests.review

import kotlinx.serialization.Serializable

@Serializable
data class GetReviewsByDishIdRequest(val dishId: Long)