package com.restaurant.models.requests.review

import kotlinx.serialization.Serializable

@Serializable
data class AddReviewRequest(val userId: Long, val dishId: Long, val rating: Int, val comment: String)