package com.restaurant

import com.restaurant.controllers.*
import com.restaurant.plugins.*
import com.restaurant.schemas.*
import com.restaurant.services.HashService
import com.restaurant.services.JwtService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val database = configureDatabases(environment)
    val userService = UserService(database)
    val dishService = DishService(database)
    val positionService = PositionService(database)
    val orderService = OrderService(database)
    val reviewService = ReviewService(database)
    val jwtService = JwtService(this, userService)
    configureSecurity(jwtService)
    configureSerialization()
    configureHTTP()
    configureRouting()
    val hashService = HashService(environment)
    configureUserController(userService, jwtService, hashService)
    configureDishController(dishService)
    configureOrderController(orderService, positionService, userService, dishService)
    configurePositionController(positionService, orderService, dishService)
    configureReviewController(reviewService, userService)
    configureStatsController(reviewService, dishService, positionService, orderService)
}
