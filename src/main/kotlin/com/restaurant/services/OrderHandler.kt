package com.restaurant.services

import com.restaurant.schemas.DishService
import com.restaurant.schemas.OrderService
import com.restaurant.schemas.OrderStatus
import com.restaurant.schemas.PositionService
import kotlinx.coroutines.*
import java.lang.Thread.sleep

class OrderHandler(
    private val orderService: OrderService,
    private val positionService: PositionService,
    private val dishService: DishService
) {
    suspend fun handle(orderId: Long) {
        runBlocking {
            val order = orderService.readById(orderId) ?: return@runBlocking
            orderService.updateStatusById(order.id, OrderStatus.PREPARING)
            println("Started handling order ${order.id}")
            // Обработка заказа
            val positions = positionService.readAllByOrderId(order.id)
            val deferredResults = mutableListOf<Deferred<Unit>>()
            for (position in positions) {
                val deferred = async {
                    println("Started handling position of dish id ${position.dishId}")
                    val dish = dishService.readById(position.dishId) ?: return@async
                    // Обработка позиции
                    positionService.updateStatusById(position.id, OrderStatus.PREPARING)
                    for (i in 0..dish.cookMinutes) {
                        delay(60 * 1000L)
                        positionService.updateMinutesLeftById(position.id, position.minutesLeft - 1)
                    }
                    positionService.updateStatusById(position.id, OrderStatus.READY)
                    println("Finished cooking dish ${position.dishId}")
                }
                deferredResults.add(deferred)
            }
            coroutineScope {
                deferredResults.forEach { it.await() }
            }
            orderService.updateStatusById(order.id, OrderStatus.READY)
            println("Finished handling order ${order.id}")
        }
    }
}