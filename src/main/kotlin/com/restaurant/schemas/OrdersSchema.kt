package com.restaurant.schemas

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

enum class OrderStatus(status: String) {
    ACCEPTED("accepted"),
    PREPARING("preparing"),
    READY("ready"),
    CANCELED("canceled"),
}

@Serializable
data class ExposedOrder(
    val id: Long,
    val customerId: Long,
    val status: OrderStatus,
    val paid: Boolean = false,
    val paidSum: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis() / 1000L
)

class OrderService(private val database: Database) {
    object Orders : Table() {
        val id = long("id").autoIncrement()
        val customerId = long("customer_id")
        val status = enumerationByName<OrderStatus>("status", 64)
        val paid = bool("paid").default(false)
        val paidSum = double("paid_sum").default(0.0)
        val createdAt = long("created_at").default(System.currentTimeMillis() / 1000L)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Orders)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(dish: ExposedOrder): Long = dbQuery {
        Orders.insert {
            it[customerId] = dish.customerId
            it[status] = dish.status
        }[Orders.id]
    }

    suspend fun readAll(): List<ExposedOrder> = dbQuery {
        Orders.selectAll().map {
            ExposedOrder(
                it[Orders.id],
                it[Orders.customerId],
                it[Orders.status],
                it[Orders.paid],
                it[Orders.paidSum],
                it[Orders.createdAt]
            )
        }
    }

    suspend fun readById(id: Long): ExposedOrder? {
        return dbQuery {
            Orders.select { Orders.id eq id }
                .map {
                    ExposedOrder(
                        it[Orders.id],
                        it[Orders.customerId],
                        it[Orders.status],
                        it[Orders.paid],
                        it[Orders.paidSum],
                        it[Orders.createdAt]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readByCustomerId(customerId: Long): List<ExposedOrder?> {
        return dbQuery {
            Orders.select { Orders.customerId eq customerId }
                .map {
                    ExposedOrder(
                        it[Orders.id],
                        it[Orders.customerId],
                        it[Orders.status],
                        it[Orders.paid],
                        it[Orders.paidSum],
                        it[Orders.createdAt]
                    )
                }
        }
    }

    suspend fun countByCustomerId(customerId: Long): Int {
        return dbQuery {
            Orders.select { Orders.customerId eq customerId }
                .map { ExposedOrder(it[Orders.id], it[Orders.customerId], it[Orders.status]) }
                .count()
        }
    }

    suspend fun countById(id: Long): Int {
        return dbQuery {
            Orders.select { Orders.id eq id }
                .map { ExposedOrder(it[Orders.id], it[Orders.customerId], it[Orders.status]) }
                .count()
        }
    }

    suspend fun updateById(id: Long, dish: ExposedOrder) {
        dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[customerId] = dish.customerId
                it[status] = dish.status
            }
        }
    }

    suspend fun updatePaidById(id: Long, paid: Boolean) {
        dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[Orders.paid] = paid
            }
        }
    }

    suspend fun updatePaidSumById(id: Long, paidSum: Double) {
        dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[Orders.paidSum] = paidSum
            }
        }
    }

    suspend fun updateStatusById(id: Long, status: OrderStatus) {
        dbQuery {
            Orders.update({ Orders.id eq id }) {
                it[Orders.status] = status
            }
        }
    }

    suspend fun delete(id: Long): Int {
        return dbQuery {
            return@dbQuery Orders.deleteWhere { Orders.id.eq(id) }
        }
    }

    suspend fun deleteByCustomerId(customerId: Long): Int {
        return dbQuery {
            return@dbQuery Orders.deleteWhere { Orders.customerId.eq(customerId) }
        }
    }

    suspend fun deleteAllReady(): Int {
        return dbQuery {
            return@dbQuery Orders.deleteWhere { status.eq(OrderStatus.READY) }
        }
    }

    suspend fun deleteAllCanceled(): Int {
        return dbQuery {
            return@dbQuery Orders.deleteWhere { status.eq(OrderStatus.CANCELED) }
        }
    }
}
