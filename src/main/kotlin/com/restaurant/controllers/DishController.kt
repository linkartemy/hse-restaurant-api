package com.restaurant.controllers

import com.restaurant.models.requests.dish.AddDishRequest
import com.restaurant.models.requests.dish.DeleteDishByIdRequest
import com.restaurant.models.requests.dish.DeleteDishByNameRequest
import com.restaurant.models.requests.dish.UpdateDishRequest
import com.restaurant.plugins.authorized
import com.restaurant.schemas.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureDishController(dishService: DishService) {
    routing {
        route("/dish") {
            authenticate {
                authorized(UserRole.ADMIN) {
                    post("/add") {
                        val request = call.receive<AddDishRequest>()
                        val name = request.name.lowercase().trim()
                        val quantity = request.quantity
                        val price = request.price
                        val cookMinutes = request.cookMinutes
                        if (dishService.countByName(name) != 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Dish with such name already exists"
                            )
                            return@post
                        }
                        if (name.isEmpty() || name.length > 100) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Name length must be more than 0 and less or equal to 100"
                            )
                            return@post
                        }
                        val id = dishService.create(
                            ExposedDish(
                                0,
                                name,
                                quantity,
                                price,
                                cookMinutes
                            )
                        )
                        call.respond(HttpStatusCode.Created, id)
                    }
                    post("/getAll") {
                        val dishes = dishService.readAll()
                        call.respond(HttpStatusCode.OK, dishes)
                    }
                    post("/deleteById") {
                        val request = call.receive<DeleteDishByIdRequest>()
                        val id = request.id
                        if (dishService.countById(id) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No dish with such id exists"
                            )
                            return@post
                        }
                        dishService.deleteById(id)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                    post("/deleteByName") {
                        val request = call.receive<DeleteDishByNameRequest>()
                        val name = request.name.lowercase().trim()
                        if (dishService.countByName(name) == 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "No dish with such name exists"
                            )
                            return@post
                        }
                        dishService.deleteByName(name)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                    post("/update") {
                        val request = call.receive<UpdateDishRequest>()
                        val id = request.id
                        val name = request.name.lowercase().trim()
                        val quantity = request.quantity
                        val price = request.price
                        val cookMinutes = request.cookMinutes
                        if (dishService.countByName(name) != 0 && dishService.readById(id)?.name != name) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Dish with such name already exists"
                            )
                            return@post
                        }
                        if (name.isEmpty() || name.length > 100) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Name length must be more than 0 and less or equal to 100"
                            )
                            return@post
                        }
                        dishService.update(
                            id,
                            ExposedDish(
                                0,
                                name,
                                quantity,
                                price,
                                cookMinutes
                            )
                        )
                        call.respond(HttpStatusCode.OK, "Updated")
                    }
                }
            }
        }
    }
}