package com.restaurant.schemas

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

val ROLES = arrayOf("admin", "visitor")

enum class UserRole(role: String) {
    ADMIN("admin"),
    CUSTOMER("customer")
}

@Serializable
data class ExposedUser(val login: String, val passwordHash: String, val passwordSalt: ByteArray, val role: UserRole)
class UserService(private val database: Database) {
    object Users : Table() {
        val id = long("id").autoIncrement()
        val login = varchar("login", 256)
        val passwordHash = varchar("password_hash", 256)
        val passwordSalt = binary("password_salt", 256)
        val role = enumerationByName<UserRole>("role", 64)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: ExposedUser): Long = dbQuery {
        Users.insert {
            it[login] = user.login
            it[passwordHash] = user.passwordHash
            it[passwordSalt] = user.passwordSalt
            it[role] = user.role
        }[Users.id]
    }

    suspend fun readAll(): List<ExposedUser> {
        return dbQuery {
            Users.selectAll()
                .map {
                    ExposedUser(
                        it[Users.login],
                        it[Users.passwordHash],
                        it[Users.passwordSalt],
                        it[Users.role]
                    )
                }
        }
    }

    suspend fun read(id: Long): ExposedUser? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { ExposedUser(it[Users.login], it[Users.passwordHash], it[Users.passwordSalt], it[Users.role]) }
                .singleOrNull()
        }
    }

    suspend fun readByLogin(login: String): ExposedUser? {
        return dbQuery {
            Users.select { Users.login.lowerCase().trim() eq login.lowercase().trim() }
                .map { ExposedUser(it[Users.login], it[Users.passwordHash], it[Users.passwordSalt], it[Users.role]) }
                .singleOrNull()
        }
    }

    suspend fun countByLogin(login: String): Int {
        return dbQuery {
            Users.select { Users.login.lowerCase().trim() eq login.lowercase().trim() }
                .map { ExposedUser(it[Users.login], it[Users.passwordHash], it[Users.passwordSalt], it[Users.role]) }
                .count()
        }
    }

    suspend fun countById(id: Long): Int {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { ExposedUser(it[Users.login], it[Users.passwordHash], it[Users.passwordSalt], it[Users.role]) }
                .count()
        }
    }

    suspend fun update(id: Long, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[login] = user.login
                it[passwordHash] = user.passwordHash
                it[passwordSalt] = user.passwordSalt
                it[role] = user.role
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    suspend fun deleteByLogin(login: String) {
        dbQuery {
            Users.deleteWhere { Users.login.lowerCase().trim() eq login.lowercase().trim() }
        }
    }
}
