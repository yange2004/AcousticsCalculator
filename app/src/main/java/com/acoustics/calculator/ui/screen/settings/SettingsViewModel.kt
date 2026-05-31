package com.acoustics.calculator.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.data.local.dao.UserDao
import com.acoustics.calculator.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentUser: UserEntity? = null,
    val showLogin: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = userDao.getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    fun showLogin() { _uiState.update { it.copy(showLogin = true) } }
    fun hideLogin() { _uiState.update { it.copy(showLogin = false) } }
    fun onLoginSuccess() { loadCurrentUser() }

    fun logout() {
        viewModelScope.launch {
            userDao.logoutAll()
            _uiState.update { it.copy(currentUser = null) }
        }
    }
}
