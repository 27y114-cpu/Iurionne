package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.repository.MediaRepository
import com.example.domain.models.*
import com.example.security.AuthManager
import com.example.security.BackupManager
import com.example.security.CryptoManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOrder { RECENTES, NOME, AVALIACAO, FAVORITOS }

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(db.encryptedWorkDao())
    private val profileDao = db.profileDao()

    // Auth screen inputs
    var usernameQuery = MutableStateFlow("")
    var passwordQuery = MutableStateFlow("")
    
    private val _authState = MutableStateFlow<String?>(null) // null, "SUCCESS", "ERROR_LOGIN", "ERROR_REGISTER"
    val authState: StateFlow<String?> = _authState

    // Active Profile reactive state
    val activeProfile = AuthManager.currentProfile

    // UI Search & Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<WorkType?>(null)
    val selectedStatus = MutableStateFlow<UserStatus?>(null)
    val selectedRatingFilter = MutableStateFlow<Int?>(null)
    val selectedSortOrder = MutableStateFlow(SortOrder.RECENTES)
    val onlyFavorites = MutableStateFlow(false)

    // Editing & Details state
    private val _selectedWork = MutableStateFlow<Work?>(null)
    val selectedWork: StateFlow<Work?> = _selectedWork

    private val _backupStatus = MutableStateFlow<String?>(null) // Toast messaging state
    val backupStatus: StateFlow<String?> = _backupStatus

    @OptIn(ExperimentalCoroutinesApi::class)
    val works: StateFlow<List<Work>> = activeProfile.flatMapLatest { profile ->
        if (profile == null) {
            flowOf(emptyList())
        } else {
            val dbKey = AuthManager.getDecryptedDbKey() ?: ""
            repository.getWorksFlow(profile.id, dbKey)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Master reactive pipeline combining reactive search, categories, and custom orderings
    val filteredWorks: StateFlow<List<Work>> = combine(
        works,
        searchQuery,
        selectedCategory,
        selectedStatus,
        selectedRatingFilter,
        selectedSortOrder,
        onlyFavorites
    ) { flowArray ->
        val worksList = flowArray[0] as List<Work>
        val query = flowArray[1] as String
        val cat = flowArray[2] as WorkType?
        val stat = flowArray[3] as UserStatus?
        val rat = flowArray[4] as Int?
        val sort = flowArray[5] as SortOrder
        val favs = flowArray[6] as Boolean

        var list = worksList
        
        if (query.isNotBlank()) {
            list = list.filter { w ->
                w.titles.any { it.title.contains(query, ignoreCase = true) } ||
                w.description.contains(query, ignoreCase = true)
            }
        }
        
        if (cat != null) {
            list = list.filter { it.type == cat }
        }
        
        if (stat != null) {
            list = list.filter { it.userStatus == stat }
        }
        
        if (rat != null) {
            list = list.filter { it.userRating == rat }
        }
        
        if (favs) {
            list = list.filter { it.favorite }
        }
        
        when (sort) {
            SortOrder.RECENTES -> list = list.sortedByDescending { it.updatedAt }
            SortOrder.NOME -> list = list.sortedBy { it.getPinnedTitle().lowercase() }
            SortOrder.AVALIACAO -> list = list.sortedByDescending { it.userRating }
            SortOrder.FAVORITOS -> list = list.sortedByDescending { it.favorite }
        }
        
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Navigation trigger or direct page navigation helpers
    private val _currentPage = MutableStateFlow("LOGIN") // LOGIN, REGISTER, RESTORE, MAIN (Dashboard)
    val currentPage: StateFlow<String> = _currentPage

    fun navigateTo(page: String) {
        _currentPage.value = page
        // clear messages
        _authState.value = null
        _backupStatus.value = null
    }

    fun clearAuthInputs() {
        usernameQuery.value = ""
        passwordQuery.value = ""
        _authState.value = null
    }

    fun handleLogin() {
        val user = usernameQuery.value
        val pass = passwordQuery.value
        if (user.isBlank() || pass.isBlank()) {
            _authState.value = "ERROR_FIELD_EMPTY"
            return
        }
        viewModelScope.launch {
            val ok = AuthManager.login(user, pass, profileDao)
            if (ok) {
                _authState.value = "SUCCESS"
                navigateTo("MAIN")
            } else {
                _authState.value = "ERROR_LOGIN"
            }
        }
    }

    fun handleRegister() {
        val user = usernameQuery.value
        val pass = passwordQuery.value
        if (user.isBlank() || pass.isBlank()) {
            _authState.value = "ERROR_FIELD_EMPTY"
            return
        }
        viewModelScope.launch {
            val ok = AuthManager.register(user, pass, profileDao)
            if (ok) {
                _authState.value = "REGISTER_SUCCESS"
                navigateTo("LOGIN")
            } else {
                _authState.value = "ERROR_REGISTER"
            }
        }
    }

    fun handleLogout() {
        AuthManager.logout()
        clearAuthInputs()
        _selectedWork.value = null
        navigateTo("LOGIN")
    }

    // Selected Work details
    fun selectWork(work: Work?) {
        _selectedWork.value = work
    }

    fun saveMediaWork(work: Work) {
        val profile = activeProfile.value ?: return
        val dbKey = AuthManager.getDecryptedDbKey() ?: return
        viewModelScope.launch {
            repository.saveWork(work, profile.id, dbKey)
            // Refresh detailed work projection if selected same
            if (_selectedWork.value?.id == work.id) {
                _selectedWork.value = work
            }
        }
    }

    fun deleteMediaWork(id: String) {
        viewModelScope.launch {
            repository.deleteWork(id)
            if (_selectedWork.value?.id == id) {
                _selectedWork.value = null
            }
        }
    }

    // Exports decrypted works in encrypted backup String format
    fun generateBackupString(): String? {
        val profile = activeProfile.value ?: return null
        val dbKey = AuthManager.getDecryptedDbKey() ?: return null
        
        // Load works synchronously block
        var exported: String? = null
        val worksList = works.value
        exported = BackupManager.exportBackup(worksList, dbKey)
        return exported
    }

    // Import Backup file
    fun restoreBackupFromString(backupJson: String): Boolean {
        val profile = activeProfile.value ?: return false
        val dbKey = AuthManager.getDecryptedDbKey() ?: return false
        
        try {
            val importedList = BackupManager.importBackup(backupJson, dbKey)
            if (importedList != null) {
                viewModelScope.launch {
                    // Overwrite/insert backup works
                    importedList.forEach { work ->
                        repository.saveWork(work, profile.id, dbKey)
                    }
                    _backupStatus.value = "RESTORE_SUCCESS"
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _backupStatus.value = "RESTORE_ERROR"
        return false
    }

    // Direct Login Restore (Restaurar Banco from Login screen)
    fun restoreBackupFromLoginScreen(backupJson: String, pass: String, user: String): Boolean {
        viewModelScope.launch {
            val profile = profileDao.getProfileByUsername(user)
            if (profile == null) {
                _backupStatus.value = "PROFILE_NOT_FOUND"
                return@launch
            }
            try {
                // Try logging in the user inline temporarily
                val salt = profile.salt
                val candidateHash = CryptoManager.hashPassword(pass, salt)
                if (candidateHash == profile.passwordHash) {
                    val parts = profile.databaseKey.split(":")
                    if (parts.size == 2) {
                        val encryptedDbKey = parts[0]
                        val iv = parts[1]
                        val derivedSpec = CryptoManager.deriveKey(pass, salt)
                        val derivedKeyB64 = android.util.Base64.encodeToString(derivedSpec.encoded, android.util.Base64.NO_WRAP)
                        val plainDbKey = CryptoManager.decrypt(encryptedDbKey, iv, derivedKeyB64)

                        val importedList = BackupManager.importBackup(backupJson, plainDbKey)
                        if (importedList != null) {
                            importedList.forEach { work ->
                                repository.saveWork(work, profile.id, plainDbKey)
                            }
                            _backupStatus.value = "RESTORE_SUCCESS"
                        } else {
                            _backupStatus.value = "RESTORE_ERROR"
                        }
                    }
                } else {
                    _backupStatus.value = "RESTORE_AUTH_FAILED"
                }
            } catch (e: Exception) {
                _backupStatus.value = "RESTORE_ERROR"
            }
        }
        return true
    }
}
