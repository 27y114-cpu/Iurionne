package com.example.data.repository

import com.example.data.dao.EncryptedWorkDao
import com.example.data.entities.EncryptedWorkEntity
import com.example.domain.models.Work
import com.example.security.CryptoManager
import com.example.utils.MoshiUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(private val encryptedWorkDao: EncryptedWorkDao) {

    fun getWorksFlow(profileId: String, dbKey: String): Flow<List<Work>> {
        return encryptedWorkDao.getWorksForProfile(profileId).map { entities ->
            entities.mapNotNull { entity ->
                try {
                    val decryptedJson = CryptoManager.decrypt(
                        entity.encryptedPayload,
                        entity.iv,
                        dbKey
                    )
                    MoshiUtils.jsonToWork(decryptedJson)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    suspend fun getWorksSync(profileId: String, dbKey: String): List<Work> {
        return encryptedWorkDao.getWorksForProfileSync(profileId).mapNotNull { entity ->
            try {
                val decryptedJson = CryptoManager.decrypt(
                    entity.encryptedPayload,
                    entity.iv,
                    dbKey
                )
                MoshiUtils.jsonToWork(decryptedJson)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun saveWork(work: Work, profileId: String, dbKey: String) {
        val updatedWork = work.copy(updatedAt = System.currentTimeMillis())
        val json = MoshiUtils.workToJson(updatedWork)
        val (encryptedPayload, iv) = CryptoManager.encrypt(json, dbKey)
        
        val entity = EncryptedWorkEntity(
            id = work.id,
            profileId = profileId,
            type = work.type.name,
            isFavorite = work.favorite,
            userStatus = work.userStatus.name,
            userRating = work.userRating,
            searchTitle = work.getPinnedTitle(),
            encryptedPayload = encryptedPayload,
            iv = iv,
            createdAt = work.createdAt,
            updatedAt = updatedWork.updatedAt
        )
        encryptedWorkDao.insertWork(entity)
    }

    suspend fun deleteWork(id: String) {
        encryptedWorkDao.deleteWorkById(id)
    }

    suspend fun clearWorks(profileId: String) {
        encryptedWorkDao.clearAllWorks(profileId)
    }
}
