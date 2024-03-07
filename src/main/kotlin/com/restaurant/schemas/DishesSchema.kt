package com.restaurant.schemas

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedDish(val id: Long, val name: String, val quantity: Int, val price: Double, val cookMinutes: Int)
class DishService(private val database: Database) {
    object Dishes : Table() {
        val id = long("id").autoIncrement()
        val name = varchar("name", 256)
        val quantity = integer("quantity")
        val price = double("price")
        val cookMinutes = integer("cook_minutes")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Dishes)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(dish: ExposedDish): Long = dbQuery {
        Dishes.insert {
            it[name] = dish.name
            it[quantity] = dish.quantity
            it[price] = dish.price
            it[cookMinutes] = dish.cookMinutes
        }[Dishes.id]
    }

    suspend fun readAll(): List<ExposedDish> = dbQuery {
        Dishes.selectAll().map {
            ExposedDish(it[Dishes.id], it[Dishes.name], it[Dishes.quantity], it[Dishes.price], it[Dishes.cookMinutes])
        }
    }

    suspend fun readById(id: Long): ExposedDish? {
        return dbQuery {
            Dishes.select { Dishes.id eq id }
                .map {
                    ExposedDish(
                        it[Dishes.id],
                        it[Dishes.name],
                        it[Dishes.quantity],
                        it[Dishes.price],
                        it[Dishes.cookMinutes]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readByName(name: String): ExposedDish? {
        return dbQuery {
            Dishes.select { Dishes.name.lowerCase().trim() eq name.lowercase().trim() }
                .map {
                    ExposedDish(
                        it[Dishes.id],
                        it[Dishes.name],
                        it[Dishes.quantity],
                        it[Dishes.price],
                        it[Dishes.cookMinutes]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun countByName(name: String): Int {
        return dbQuery {
            Dishes.select { Dishes.name.lowerCase().trim() eq name.lowercase().trim() }
                .map {
                    ExposedDish(
                        it[Dishes.id],
                        it[Dishes.name],
                        it[Dishes.quantity],
                        it[Dishes.price],
                        it[Dishes.cookMinutes]
                    )
                }
                .count()
        }
    }

    suspend fun countById(id: Long): Int {
        return dbQuery {
            Dishes.select { Dishes.id eq id }
                .map {
                    ExposedDish(
                        it[Dishes.id],
                        it[Dishes.name],
                        it[Dishes.quantity],
                        it[Dishes.price],
                        it[Dishes.cookMinutes]
                    )
                }
                .count()
        }
    }

    suspend fun update(id: Long, dish: ExposedDish) {
        dbQuery {
            Dishes.update({ Dishes.id eq id }) {
                it[name] = dish.name
                it[quantity] = dish.quantity
                it[price] = dish.price
                it[cookMinutes] = dish.cookMinutes
            }
        }
    }

    suspend fun updateQuantityById(id: Long, quantity: Int) {
        dbQuery {
            Dishes.update({ Dishes.id eq id }) {
                it[Dishes.quantity] = quantity
            }
        }
    }

    suspend fun deleteById(id: Long) {
        dbQuery {
            Dishes.deleteWhere { Dishes.id.eq(id) }
        }
    }

    suspend fun deleteByName(name: String) {
        dbQuery {
            Dishes.deleteWhere { Dishes.name.lowerCase().trim() eq name.lowercase().trim() }
        }
    }
}
