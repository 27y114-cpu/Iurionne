package com.example.data.dao

import androidx.room.*
import com.example.data.entities.EncryptedWorkEntity
import com.example.data.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE username = :username LIMIT 1")
    suspend fun getProfileByUsername(username: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles ORDER BY createdAt DESC")
    suspend fun getAllProfiles(): List<ProfileEntity>
}

@Dao
interface EncryptedWorkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWork(work: EncryptedWorkEntity)

    @Query("DELETE FROM encrypted_works WHERE id = :id")
    suspend fun deleteWorkById(id: String)

    @Query("SELECT * FROM encrypted_works WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun getWorksForProfile(profileId: String): Flow<List<EncryptedWorkEntity>>

    @Query("SELECT * FROM encrypted_works WHERE profileId = :profileId ORDER BY updatedAt DESC")
    suspend fun getWorksForProfileSync(profileId: String): List<EncryptedWorkEntity>

    @Query("DELETE FROM encrypted_works WHERE profileId = :profileId")
    suspend fun clearAllWorks(profileId: String)
}
