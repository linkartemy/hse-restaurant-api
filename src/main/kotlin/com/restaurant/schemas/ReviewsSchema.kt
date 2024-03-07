package com.restaurant.schemas

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedReview(val userId: Long, val dishId: Long, val rating: Int, val comment: String)
class ReviewService(private val database: Database) {
    object Reviews : Table() {
        val id = long("id").autoIncrement()
        val userId = long("user_id")
        val dishId = long("dish_id")
        val rating = integer("rating")
        val comment = varchar("comment", 256)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Reviews)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(review: ExposedReview): Long = dbQuery {
        Reviews.insert {
            it[userId] = review.userId
            it[dishId] = review.dishId
            it[rating] = review.rating
            it[comment] = review.comment
        }[Reviews.id]
    }

    suspend fun readById(id: Long): ExposedReview? {
        return dbQuery {
            Reviews.select { Reviews.id eq id }
                .map { ExposedReview(it[Reviews.userId], it[Reviews.dishId], it[Reviews.rating], it[Reviews.comment]) }
                .singleOrNull()
        }
    }

    suspend fun readByUserId(userId: Long): ExposedReview? {
        return dbQuery {
            Reviews.select { Reviews.userId eq userId }
                .map { ExposedReview(it[Reviews.userId], it[Reviews.dishId], it[Reviews.rating], it[Reviews.comment]) }
                .singleOrNull()
        }
    }

    suspend fun readByDishId(dishId: Long): List<ExposedReview> {
        return dbQuery {
            Reviews.select { Reviews.dishId eq dishId }
                .map { ExposedReview(it[Reviews.userId], it[Reviews.dishId], it[Reviews.rating], it[Reviews.comment]) }
        }
    }

    suspend fun countByUserId(userId: Long): Int {
        return dbQuery {
            Reviews.select { Reviews.userId eq userId }
                .map { ExposedReview(it[Reviews.userId], it[Reviews.dishId], it[Reviews.rating], it[Reviews.comment]) }
                .count()
        }
    }

    suspend fun countById(id: Long): Int {
        return dbQuery {
            Reviews.select { Reviews.id eq id }
                .map { ExposedReview(it[Reviews.userId], it[Reviews.dishId], it[Reviews.rating], it[Reviews.comment]) }
                .count()
        }
    }

    suspend fun update(id: Long, review: ExposedReview) {
        dbQuery {
            Reviews.update({ Reviews.id eq id }) {
                it[userId] = review.userId
                it[dishId] = review.dishId
                it[rating] = review.rating
                it[comment] = review.comment
            }
        }
    }

    suspend fun deleteById(id: Long) {
        dbQuery {
            Reviews.deleteWhere { Reviews.id.eq(id) }
        }
    }

    suspend fun deleteByUserId(userId: Long): Int {
        return dbQuery {
            return@dbQuery Reviews.deleteWhere { Reviews.userId eq userId }
        }
    }
}
