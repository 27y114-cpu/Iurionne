package com.example.security

import com.example.domain.models.Work
import com.example.utils.MoshiUtils
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@JsonClass(generateAdapter = true)
data class BackupContainer(
    val version: Int,
    val encryptedData: String,
    val metadata: Map<String, String>
)

object BackupManager {

    private val containerAdapter = MoshiUtils.moshi.adapter(BackupContainer::class.java)
    
    @Suppress("UNCHECKED_CAST")
    private val worksListAdapter = MoshiUtils.moshi.adapter<List<Work>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, Work::class.java)
    )

    fun exportBackup(works: List<Work>, dbKey: String): String? {
        return try {
            val listJson = worksListAdapter.toJson(works)
            val (encrypted, iv) = CryptoManager.encrypt(listJson, dbKey)
            
            val metadata = mapOf(
                "iv" to iv,
                "exportedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "itemsCount" to works.size.toString()
            )
            
            val container = BackupContainer(
                version = 1,
                encryptedData = encrypted,
                metadata = metadata
            )
            
            containerAdapter.toJson(container)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importBackup(backupJson: String, dbKey: String): List<Work>? {
        return try {
            val container = containerAdapter.fromJson(backupJson) ?: return null
            if (container.version != 1) return null
            
            val iv = container.metadata["iv"] ?: return null
            val decryptedJson = CryptoManager.decrypt(container.encryptedData, iv, dbKey)
            
            worksListAdapter.fromJson(decryptedJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
