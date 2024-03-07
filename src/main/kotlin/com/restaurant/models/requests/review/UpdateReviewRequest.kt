package com.restaurant.models.requests.review

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReviewRequest(val id: Long, val userId: Long, val dishId: Long, val rating: Int, val comment: String)