package com.restaurant.controllers

import com.restaurant.models.requests.order.*
import com.restaurant.plugins.authorized
import com.restaurant.schemas.*
import com.restaurant.services.OrderHandler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Application.configureOrderController(
    orderService: OrderService,
    positionService: PositionService,
    userService: UserService,
    dishService: DishService
) {
    routing {
        route("/order") {
            authenticate {
                authorized(UserRole.ADMIN) {
                    post("/add") {
                        val request = call.receive<AddOrderRequest>()
                        val customerId = request.customerId
                        if (userService.countById(customerId) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No user with such id exists"
                            )
                            return@post
                        }
                        val id = orderService.create(
                            ExposedOrder(
                                0,
                                customerId,
                                OrderStatus.ACCEPTED
                            )
                        )
                        call.respond(HttpStatusCode.Created, id)
                    }
                    post("/getAll") {
                        val orders = orderService.readAll()
                        call.respond(HttpStatusCode.OK, orders)
                    }
                    post("/getById") {
                        val request = call.receive<GetOrderByIdRequest>()
                        val id = request.id
                        val order = orderService.readById(id)
                        if (order == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        call.respond(HttpStatusCode.OK, order)
                    }
                    post("/serve") {
                        val request = call.receive<ServeOrderByIdRequest>()
                        val id = request.id
                        val order = orderService.readById(id)
                        if (order == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        if (order.status != OrderStatus.ACCEPTED) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Order isn't accepted, you can't serve it"
                            )
                            return@post
                        }
                        val positions = positionService.countByOrderId(id)
                        if (positions == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Order is empty, you can't serve it"
                            )
                            return@post
                        }
                        val orderHandler = OrderHandler(orderService, positionService, dishService)
                        call.respond(HttpStatusCode.OK, "Served")
                        GlobalScope.launch {
                            orderHandler.handle(id)
                        }
                    }
                    post("/updateOrderStatusById") {
                        val request = call.receive<UpdateOrderStatusByIdRequest>()
                        val id = request.id
                        val status = request.status
                        if (orderService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        if (status == OrderStatus.READY) {
                            val positions = positionService.readAllByOrderId(id)
                            if (positions.any { it.status != OrderStatus.READY }) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    "Not all positions are ready"
                                )
                                return@post
                            }
                        }
                        orderService.updateStatusById(id, status)
                        positionService.updateStatusByOrderId(id, status)
                        call.respond(HttpStatusCode.OK, "Updated")
                    }
                    post("/deleteAllReadyOrders") {
                        val ordersCount = orderService.deleteAllReady()
                        val positionsCount = positionService.deleteAllReady()
                        call.respond(HttpStatusCode.OK, "Deleted $ordersCount orders and $positionsCount positions")
                    }
                    post("/deleteAllCanceledOrders") {
                        val orderCount = orderService.deleteAllCanceled()
                        val positionsCount = positionService.deleteAllCanceled()
                        call.respond(HttpStatusCode.OK, "Deleted $orderCount orders and $positionsCount positions")
                    }
                    post("/deleteByCustomerId") {
                        val request = call.receive<DeleteOrderByCustomerIdRequest>()
                        val customerId = request.customerId
                        val orders = orderService.readByCustomerId(customerId)
                        if (orders.isEmpty() || orders[0] == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such customer id exists"
                            )
                            return@post
                        }
                        var positionsCount = 0
                        for (order in orders) {
                            if (order == null) continue
                            positionsCount += positionService.deleteByOrderId(order.id)
                        }
                        val ordersCount = orderService.deleteByCustomerId(customerId)
                        call.respond(HttpStatusCode.OK, "Deleted $ordersCount orders and $positionsCount positions")
                    }
                    post("/deleteById") {
                        val request = call.receive<DeleteOrderByIdRequest>()
                        val id = request.id
                        if (orderService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        val ordersCount = orderService.delete(id)
                        val positionsCount = positionService.deleteByOrderId(id)
                        call.respond(HttpStatusCode.OK, "Deleted $ordersCount orders and $positionsCount positions")
                    }
                }
            }
            authenticate {
                authorized(UserRole.ADMIN, UserRole.CUSTOMER) {
                    post("/getStatusById") {
                        val request = call.receive<GetStatusByIdRequest>()
                        val id = request.id
                        val order = orderService.readById(id)
                        if (order == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        call.respond(HttpStatusCode.OK, order.status)
                    }
                    post("/pay") {
                        val request = call.receive<PayOrderByIdRequest>()
                        val id = request.id
                        val paymentSum = request.paymentSum
                        val order = orderService.readById(id)
                        if (order == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        if (order.status != OrderStatus.READY) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Order isn't ready, you can't pay for it"
                            )
                            return@post
                        }
                        if (order.paid) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Order is already paid"
                            )
                            return@post
                        }
                        val positionsSum = positionService.readAllByOrderId(id).sumOf {
                            dishService.readById(it.dishId)?.price ?: 0.0
                        }
                        if (paymentSum < positionsSum) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Not enough money to pay for the order"
                            )
                            return@post
                        }
                        orderService.updatePaidById(id, true)
                        val change = paymentSum - positionsSum
                        orderService.updatePaidSumById(id, positionsSum)
                        call.respond(HttpStatusCode.OK, "Paid $paymentSum, change $change")
                    }
                    post("/cancel") {
                        val request = call.receive<CancelOrderByIdRequest>()
                        val id = request.id
                        val order = orderService.readById(id)
                        if (order == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No order with such id exists"
                            )
                            return@post
                        }
                        if (order.status == OrderStatus.READY) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Order is ready, you can't cancel it"
                            )
                            return@post
                        }
                        orderService.updateStatusById(id, OrderStatus.CANCELED)
                        positionService.updateStatusByOrderId(id, OrderStatus.CANCELED)
                        val dishes = positionService.readAllByOrderId(id).mapNotNull {
                            dishService.readById(it.dishId)
                        }
                        for (dish in dishes) {
                            dishService.updateQuantityById(dish.id, dish.quantity + 1)
                        }
                        call.respond(HttpStatusCode.OK, "Canceled")
                    }
                }
            }
        }
    }
}