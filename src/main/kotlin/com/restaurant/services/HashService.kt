package com.restaurant.services

import io.ktor.server.application.*
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class HashService(environment: ApplicationEnvironment) {
    private val algorithm = environment.config.property("hash.algorithm").getString()
    private val iterations = environment.config.property("hash.iterations").getString().toInt()
    private val keyLength = environment.config.property("hash.keyLength").getString().toInt()
    private val secret = environment.config.property("hash.secret").getString()

    private val random = SecureRandom()

    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    fun verify(password: String, salt: ByteArray, expectedHash: String): Boolean = hash(password, salt) == expectedHash

    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        try {
            val skf = SecretKeyFactory.getInstance(algorithm)
            return skf.generateSecret(spec).encoded.toString(Charset.defaultCharset())
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError("Error while hashing a password: " + e.message, e)
        } catch (e: InvalidKeySpecException) {
            throw AssertionError("Error while hashing a password: " + e.message, e)
        } finally {
            spec.clearPassword()
        }
    }
}