package com.restaurant.controllers

import com.restaurant.models.requests.dish.AddDishRequest
import com.restaurant.models.requests.dish.DeleteDishByIdRequest
import com.restaurant.models.requests.dish.DeleteDishByNameRequest
import com.restaurant.models.requests.dish.UpdateDishRequest
import com.restaurant.models.requests.review.AddReviewRequest
import com.restaurant.models.requests.review.DeleteReviewByIdRequest
import com.restaurant.models.requests.review.DeleteReviewByUserIdRequest
import com.restaurant.models.requests.review.UpdateReviewRequest
import com.restaurant.models.requests.stats.GetMostPopularDishesRequest
import com.restaurant.models.requests.stats.GetOrdersCountByPeriodRequest
import com.restaurant.plugins.authorized
import com.restaurant.schemas.*
import com.restaurant.services.DateService
import com.restaurant.services.addDays
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.DateFormat
import java.util.*

fun Application.configureStatsController(
    reviewService: ReviewService,
    dishService: DishService,
    positionService: PositionService,
    orderService: OrderService
) {
    routing {
        route("/stats") {
            authenticate {
                // Реализовать функционал, позволяющий администратору просматривать статистику по заказам и отзывам
                // (например, самые популярные блюда, средняя оценка блюд, количество заказов за период).
                authorized(UserRole.ADMIN) {
                    post("/getMostPopularDishes") {
                        val request = call.receive<GetMostPopularDishesRequest>()
                        val count = request.count
                        val positions = positionService.readAll()
                        val dishes = mutableMapOf<ExposedDish, Int>()
                        for (position in positions) {
                            val dish = dishService.readById(position.dishId) ?: continue
                            if (dishes.containsKey(dish)) {
                                dishes[dish] = dishes[dish]!! + 1
                            } else {
                                dishes[dish] = 1
                            }
                        }
                        val sortedDishes = dishes.toList().sortedByDescending { (_, value) -> value }.toMap()
                        val result = sortedDishes.keys.take(count)
                        call.respond(HttpStatusCode.OK, result)
                    }
                    post("/getAverageDishRating") {
                        val dishes = dishService.readAll()
                        val dishRatings = mutableMapOf<ExposedDish, MutableList<Int>>()
                        for (dish in dishes) {
                            val reviews = reviewService.readByDishId(dish.id)
                            val ratings = reviews.map { it.rating }
                            dishRatings[dish] = ratings.toMutableList()
                        }
                        val result = dishRatings.mapValues { (_, value) -> value.average() }
                        call.respond(HttpStatusCode.OK, result)
                    }
                    post("/getOrdersCountByPeriod") {
                        val request = call.receive<GetOrdersCountByPeriodRequest>()
                        val startDate = DateService.parseDate(request.startDate)
                        val endDate = DateService.parseDate(request.endDate).addDays(1)
                        val orders = orderService.readAll()
                        var count = 0
                        for (order in orders) {
                            val orderDate = DateService.parseDate(order.createdAt)
                            if (orderDate in startDate..endDate) {
                                ++count
                            }
                        }
                        call.respond(HttpStatusCode.OK, count)
                    }
                }
            }
        }
    }
}