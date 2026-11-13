package com.novaaforgee.profilemaster.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novaaforgee.profilemaster.model.InstalledApp
import com.novaaforgee.profilemaster.model.ProvisionedSpaceInfo
import com.novaaforgee.profilemaster.provisioner.SystemProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val activeSpaces: List<ProvisionedSpaceInfo> = emptyList(),
    val installedApps: List<InstalledApp> = emptyList(),
    val selectedApp: InstalledApp? = null,
    val selectedSpaceId: Int = 10,
    val isLoading: Boolean = false,
    val copyAdbCommandText: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val profileManager = SystemProfileManager(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val spaces = profileManager.detectActiveSpaces()
            val apps = profileManager.getInstalledUserApps()
            val targetId = spaces.firstOrNull { it.userId != 0 }?.userId ?: 10

            _uiState.value = _uiState.value.copy(
                activeSpaces = spaces,
                installedApps = apps,
                selectedSpaceId = targetId,
                isLoading = false
            )
        }
    }

    fun selectApp(app: InstalledApp) {
        val cmd = profileManager.getAdbCloneCommand(app.packageName, _uiState.value.selectedSpaceId)
        _uiState.value = _uiState.value.copy(selectedApp = app, copyAdbCommandText = cmd)
    }

    fun selectSpace(userId: Int) {
        val cmd = _uiState.value.selectedApp?.let {
            profileManager.getAdbCloneCommand(it.packageName, userId)
        }
        _uiState.value = _uiState.value.copy(selectedSpaceId = userId, copyAdbCommandText = cmd)
    }

    fun clearSelectedApp() {
        _uiState.value = _uiState.value.copy(selectedApp = null, copyAdbCommandText = null)
    }
}
