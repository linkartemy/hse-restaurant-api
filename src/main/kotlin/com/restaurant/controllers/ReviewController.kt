package com.restaurant.controllers

import com.restaurant.models.requests.dish.AddDishRequest
import com.restaurant.models.requests.dish.DeleteDishByIdRequest
import com.restaurant.models.requests.dish.DeleteDishByNameRequest
import com.restaurant.models.requests.dish.UpdateDishRequest
import com.restaurant.models.requests.review.*
import com.restaurant.plugins.authorized
import com.restaurant.schemas.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureReviewController(reviewService: ReviewService, userService: UserService) {
    routing {
        route("/review") {
            authenticate {
                authorized(UserRole.ADMIN) {
                    post("/getReviewsByDishId") {
                        val request = call.receive<GetReviewsByDishIdRequest>()
                        val dishId = request.dishId
                        val reviews = reviewService.readByDishId(dishId)
                        call.respond(HttpStatusCode.OK, reviews)
                    }
                    post("/deleteById") {
                        val request = call.receive<DeleteReviewByIdRequest>()
                        val id = request.id
                        if (reviewService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No review with such id exists"
                            )
                            return@post
                        }
                        reviewService.deleteById(id)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                    post("/deleteByUserId") {
                        val request = call.receive<DeleteReviewByUserIdRequest>()
                        val userId = request.userId
                        if (userService.countById(userId) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No user with such id exists"
                            )
                            return@post
                        }
                        val count = reviewService.deleteByUserId(userId)
                        call.respond(HttpStatusCode.OK, "Deleted $count reviews")
                    }
                    post("/update") {
                        val request = call.receive<UpdateReviewRequest>()
                        val id = request.id
                        val userId = request.userId
                        val dishId = request.dishId
                        val rating = request.rating
                        val comment = request.comment
                        if (reviewService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No review with such id exists"
                            )
                            return@post
                        }
                        if (userService.countById(userId) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No user with such id exists"
                            )
                            return@post
                        }
                        if (rating < 1 || rating > 5) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Rating must be between 1 and 5"
                            )
                            return@post
                        }
                        reviewService.update(
                            id,
                            ExposedReview(
                                userId,
                                dishId,
                                rating,
                                comment
                            )
                        )
                        call.respond(HttpStatusCode.OK, "Updated")
                    }
                }
            }
            authenticate {
                authorized(UserRole.ADMIN, UserRole.CUSTOMER) {
                    post("/add") {
                        val request = call.receive<AddReviewRequest>()
                        val userId = request.userId
                        val dishId = request.dishId
                        val rating = request.rating
                        val comment = request.comment
                        if (userService.countById(userId) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No user with such id exists"
                            )
                            return@post
                        }
                        if (rating < 1 || rating > 5) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Rating must be between 1 and 5"
                            )
                            return@post
                        }
                        val id = reviewService.create(
                            ExposedReview(
                                userId,
                                dishId,
                                rating,
                                comment
                            )
                        )
                        call.respond(HttpStatusCode.Created, id)
                    }
                }
            }
        }
    }
}