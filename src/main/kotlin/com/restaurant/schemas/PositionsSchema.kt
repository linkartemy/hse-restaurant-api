package com.restaurant.schemas

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedPosition(
    val id: Long,
    val orderId: Long,
    val dishId: Long,
    val minutesLeft: Int,
    val status: OrderStatus = OrderStatus.ACCEPTED
)

class PositionService(private val database: Database) {
    object Positions : Table() {
        val id = long("id").autoIncrement()
        val orderId = long("order_id")
        val dishId = long("dish_id")
        val minutesLeft = integer("minutes_left")
        val status = enumerationByName<OrderStatus>("status", 64).default(OrderStatus.ACCEPTED)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Positions)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(dish: ExposedPosition): Long = dbQuery {
        Positions.insert {
            it[orderId] = dish.orderId
            it[dishId] = dish.dishId
            it[minutesLeft] = dish.minutesLeft
            it[status] = dish.status
        }[Positions.id]
    }

    suspend fun readAll(): List<ExposedPosition> {
        return dbQuery {
            Positions.selectAll()
                .map {
                    ExposedPosition(
                        it[Positions.id],
                        it[Positions.orderId],
                        it[Positions.dishId],
                        it[Positions.minutesLeft],
                        it[Positions.status]
                    )
                }
        }
    }

    suspend fun readAllByOrderId(orderId: Long): List<ExposedPosition> {
        return dbQuery {
            Positions.select { Positions.orderId eq orderId }
                .map {
                    ExposedPosition(
                        it[Positions.id],
                        it[Positions.orderId],
                        it[Positions.dishId],
                        it[Positions.minutesLeft],
                        it[Positions.status]
                    )
                }
        }
    }

    suspend fun countById(id: Long): Int {
        return dbQuery {
            Positions.select { Positions.id eq id }
                .map {
                    ExposedPosition(
                        it[Positions.id],
                        it[Positions.orderId],
                        it[Positions.dishId],
                        it[Positions.minutesLeft],
                        it[Positions.status]
                    )
                }
                .count()
        }
    }

    suspend fun countByName(orderId: Long): Int {
        return dbQuery {
            Positions.select { Positions.orderId eq orderId }
                .map {
                    ExposedPosition(
                        it[Positions.id],
                        it[Positions.orderId],
                        it[Positions.dishId],
                        it[Positions.minutesLeft],
                        it[Positions.status]
                    )
                }
                .count()
        }
    }

    suspend fun countByOrderIdAndDishId(orderId: Long, dishId: Long): Int {
        return dbQuery {
            Positions.select { (Positions.orderId eq orderId) and (Positions.dishId eq dishId) }
                .map {
                    ExposedPosition(
                        it[Positions.id],
                        it[Positions.orderId],
                        it[Positions.dishId],
                        it[Positions.minutesLeft],
                        it[Positions.status]
                    )
                }
                .count()
        }
    }

    suspend fun countByOrderId(orderId: Long): Int {
        return dbQuery {
            Positions.select { Positions.orderId eq orderId }
                .map {
                    ExposedPosition(
                        it[Positions.id],
                        it[Positions.orderId],
                        it[Positions.dishId],
                        it[Positions.minutesLeft],
                        it[Positions.status]
                    )
                }
                .count()
        }
    }

    suspend fun update(id: Long, dish: ExposedPosition) {
        dbQuery {
            Positions.update({ Positions.id eq id }) {
                it[orderId] = dish.orderId
                it[dishId] = dish.dishId
                it[minutesLeft] = dish.minutesLeft
                it[status] = dish.status
            }
        }
    }

    suspend fun updateStatusByOrderId(orderId: Long, status: OrderStatus) {
        dbQuery {
            Positions.update({ Positions.orderId eq orderId }) {
                it[Positions.status] = status
            }
        }
    }

    suspend fun updateStatusById(id: Long, status: OrderStatus) {
        dbQuery {
            Positions.update({ Positions.id eq id }) {
                it[Positions.status] = status
            }
        }
    }

    suspend fun updateMinutesLeftById(id: Long, minutesLeft: Int) {
        dbQuery {
            Positions.update({ Positions.id eq id }) {
                it[Positions.minutesLeft] = minutesLeft
            }
        }
    }

    suspend fun deleteById(id: Long) {
        dbQuery {
            Positions.deleteWhere { Positions.id.eq(id) }
        }
    }

    suspend fun deleteByOrderId(orderId: Long): Int {
        return dbQuery {
            return@dbQuery Positions.deleteWhere { Positions.orderId.eq(orderId) }
        }
    }

    suspend fun deleteAllReady(): Int {
        return dbQuery {
            return@dbQuery Positions.deleteWhere { status.eq(OrderStatus.READY) }
        }
    }

    suspend fun deleteAllCanceled(): Int {
        return dbQuery {
            return@dbQuery Positions.deleteWhere { status.eq(OrderStatus.CANCELED) }
        }
    }
}
