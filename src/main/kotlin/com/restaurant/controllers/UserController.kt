package com.restaurant.controllers

import com.restaurant.models.requests.user.DeleteUserByIdRequest
import com.restaurant.models.requests.user.RegisterRequest
import com.restaurant.models.requests.user.GetUserByLoginRequest
import com.restaurant.models.requests.user.LoginRequest
import com.restaurant.plugins.RoleBasedAuthorizationPlugin
import com.restaurant.plugins.authorized
import com.restaurant.schemas.ExposedUser
import com.restaurant.schemas.UserRole
import com.restaurant.schemas.UserService
import com.restaurant.services.HashService
import com.restaurant.services.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureUserController(userService: UserService, jwtService: JwtService, hashService: HashService) {
    routing {
        route("/user") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val role = request.role
                val userRole = UserRole.entries.firstOrNull { it == role }
                if (userRole == null) {
                    call.respond(HttpStatusCode.BadRequest, "No such role exists")
                }
                if (userService.countByLogin(request.login) != 0) {
                    call.respond(HttpStatusCode.BadRequest, "User with such login already exists")
                    return@post
                }
                val salt = hashService.generateSalt()
                val user = ExposedUser(
                    request.login,
                    hashService.hash(request.password, salt),
                    salt,
                    userRole!!
                )
                val id =
                    userService.create(user)
                val token = jwtService.createJwtToken(user)
                call.response.header(name = "token", value = token)
                call.respond(HttpStatusCode.Created, id)
            }
            post("/login") {
                val request = call.receive<LoginRequest>()
                val login = request.login
                val password = request.password.trim()
                val user = userService.readByLogin(login)
                if (user == null || !hashService.verify(password, user.passwordSalt, user.passwordHash)) {
                    call.respond(HttpStatusCode.BadRequest, "Login or password is incorrect")
                    return@post
                }
                val token = jwtService.createJwtToken(user)
                call.response.header(name = "token", value = token)
                call.respond(HttpStatusCode.Created, token)
            }
            authenticate {
                authorized(UserRole.ADMIN) {
                    post("/getAllUsers") {
                        val users = userService.readAll()
                        call.respond(HttpStatusCode.OK, users)
                    }
                    post("/getByLogin") {
                        val request = call.receive<GetUserByLoginRequest>()
                        val user = userService.readByLogin(request.login)
                        if (user == null) {
                            call.respond(HttpStatusCode.BadRequest, "User with such login doesn't exist")
                            return@post
                        }
                        call.respond(HttpStatusCode.Found, user)
                    }
                    post("/deleteById") {
                        val request = call.receive<DeleteUserByIdRequest>()
                        val id = request.id
                        if (userService.countById(id) == 0) {
                            call.respond(HttpStatusCode.BadRequest, "No user with such id exists")
                            return@post
                        }
                        userService.delete(id)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                    post("/deleteByLogin") {
                        val request = call.receive<GetUserByLoginRequest>()
                        val login = request.login
                        if (userService.countByLogin(login) == 0) {
                            call.respond(HttpStatusCode.BadRequest, "No user with such login exists")
                            return@post
                        }
                        userService.deleteByLogin(login)
                        call.respond(HttpStatusCode.OK, "Deleted")
                    }
                }
            }
        }
    }
}
