package com.example.security

import android.util.Base64
import com.example.data.dao.ProfileDao
import com.example.data.entities.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

object AuthManager {
    private val _currentProfile = MutableStateFlow<ProfileEntity?>(null)
    val currentProfile: StateFlow<ProfileEntity?> = _currentProfile

    private var decryptedDbKey: String? = null

    fun getDecryptedDbKey(): String? {
        return decryptedDbKey
    }

    fun logout() {
        decryptedDbKey = null
        _currentProfile.value = null
    }

    suspend fun register(username: String, password: String, profileDao: ProfileDao): Boolean {
        if (username.isBlank() || password.isBlank()) return false
        // Check if username already exists
        val existing = profileDao.getProfileByUsername(username)
        if (existing != null) return false

        try {
            val salt = CryptoManager.generateSalt()
            // Derive PBKDF2 master key for password hashing
            val passwordHash = CryptoManager.hashPassword(password, salt)

            // Derive key spec to encrypt the newly generated database master key
            val derivedSpec = CryptoManager.deriveKey(password, salt)
            val derivedKeyB64 = Base64.encodeToString(derivedSpec.encoded, Base64.NO_WRAP)

            // Generate secure master key for AES-GCM data encryption
            val randomDbKey = CryptoManager.generateRandomKey()
            val (encryptedDbKey, iv) = CryptoManager.encrypt(randomDbKey, derivedKeyB64)
            val databaseKeyStored = "$encryptedDbKey:$iv"

            val profile = ProfileEntity(
                id = UUID.randomUUID().toString(),
                username = username,
                passwordHash = passwordHash,
                salt = salt,
                databaseKey = databaseKeyStored,
                createdAt = System.currentTimeMillis()
            )
            profileDao.insertProfile(profile)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun login(username: String, password: String, profileDao: ProfileDao): Boolean {
        if (username.isBlank() || password.isBlank()) return false
        val profile = profileDao.getProfileByUsername(username) ?: return false

        try {
            val candidateHash = CryptoManager.hashPassword(password, profile.salt)
            if (candidateHash == profile.passwordHash) {
                // Password matches, let's decrypt the database key
                val parts = profile.databaseKey.split(":")
                if (parts.size == 2) {
                    val encryptedDbKey = parts[0]
                    val iv = parts[1]

                    val derivedSpec = CryptoManager.deriveKey(password, profile.salt)
                    val derivedKeyB64 = Base64.encodeToString(derivedSpec.encoded, Base64.NO_WRAP)

                    val plainDbKey = CryptoManager.decrypt(encryptedDbKey, iv, derivedKeyB64)
                    decryptedDbKey = plainDbKey
                    _currentProfile.value = profile
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
