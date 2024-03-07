package com.restaurant.controllers

import com.restaurant.models.requests.order.DeleteOrderByIdRequest
import com.restaurant.models.requests.order.GetAllPositionsByOrderIdRequest
import com.restaurant.models.requests.position.AddPositionRequest
import com.restaurant.models.requests.position.DeletePositionByIdRequest
import com.restaurant.models.requests.position.DeletePositionByOrderIdRequest
import com.restaurant.models.requests.position.UpdatePositionStatusByIdRequest
import com.restaurant.plugins.authorized
import com.restaurant.schemas.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configurePositionController(
    positionService: PositionService,
    orderService: OrderService,
    dishService: DishService
) {
    routing {
        route("/position") {
            authenticate {
                authorized(UserRole.ADMIN) {
                    post("/add") {
                        val request = call.receive<AddPositionRequest>()
                        val orderId = request.orderId
                        val dishId = request.dishId
                        val order = orderService.readById(orderId)
                        if (order == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No user with such id exists"
                            )
                            return@post
                        }
                        val dish = dishService.readById(dishId)
                        if (dish == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No dish with such id exists"
                            )
                            return@post
                        }
                        if (dish.quantity == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Dish is out of stock"
                            )
                            return@post
                        }
                        if (order.status == OrderStatus.READY) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Order is ready, you can't add new positions"
                            )
                            return@post
                        }
                        val id = positionService.create(
                            ExposedPosition(
                                0,
                                orderId,
                                dishId,
                                dish.cookMinutes
                            )
                        )
                        dishService.updateQuantityById(dishId, dish.quantity - 1)
                        call.respond(HttpStatusCode.Created, id)
                    }
                    post("/getAllPositions") {
                        val positions = positionService.readAll()
                        call.respond(HttpStatusCode.OK, positions)
                    }
                    post("/getAllPositionsByOrderId") {
                        val request = call.receive<GetAllPositionsByOrderIdRequest>()
                        val orderId = request.orderId
                        if (orderService.countById(orderId) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        val positions = positionService.readAllByOrderId(orderId)
                        call.respond(HttpStatusCode.OK, positions)
                    }
                    post("/updateStatus") {
                        val request = call.receive<UpdatePositionStatusByIdRequest>()
                        val id = request.id
                        val status = request.status
                        if (positionService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No position with such id exists"
                            )
                            return@post
                        }
                        positionService.updateStatusById(id, status)
                        call.respond(HttpStatusCode.OK, "Updated")
                    }
                    post("/deleteById") {
                        val request = call.receive<DeletePositionByIdRequest>()
                        val id = request.id
                        if (positionService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No position with such id exists"
                            )
                            return@post
                        }
                        positionService.deleteById(id)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                    post("/deleteByOrderId") {
                        val request = call.receive<DeletePositionByOrderIdRequest>()
                        val orderId = request.orderId
                        if (positionService.countByOrderId(orderId) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such customer id exists"
                            )
                            return@post
                        }
                        positionService.deleteByOrderId(orderId)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                }
            }
        }
    }
}