package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val databaseKey: String, // encrypted with user derived PIN/password
    val createdAt: Long
)

@Entity(tableName = "encrypted_works")
data class EncryptedWorkEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val type: String, // SERIES, FILMES, ANIMES, LIVROS, HQS
    val isFavorite: Boolean,
    val userStatus: String,
    val userRating: Int,
    val searchTitle: String,
    val encryptedPayload: String, // AES-GCM encrypted JSON representation of Work
    val iv: String,
    val createdAt: Long,
    val updatedAt: Long
)
