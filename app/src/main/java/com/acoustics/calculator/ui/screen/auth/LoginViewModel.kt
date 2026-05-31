package com.acoustics.calculator.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.data.local.dao.UserDao
import com.acoustics.calculator.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val phone: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val loginMessage: String? = null,
    val currentUser: UserEntity? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingLogin()
    }

    private fun checkExistingLogin() {
        viewModelScope.launch {
            val existing = userDao.getCurrentUser()
            if (existing != null) {
                _uiState.update {
                    it.copy(isLoggedIn = true, currentUser = existing)
                }
            }
        }
    }

    fun updatePhone(phone: String) {
        // Only allow digits
        val digits = phone.filter { it.isDigit() }.take(11)
        _uiState.update { it.copy(phone = digits, error = null, loginMessage = null) }
    }

    fun updateNickname(name: String) {
        _uiState.update { it.copy(nickname = name) }
    }

    fun login() {
        val phone = _uiState.value.phone
        if (phone.length < 11) {
            _uiState.update { it.copy(error = "请输入完整的11位手机号码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, loginMessage = null) }

            try {
                val existing = userDao.findByPhone(phone)
                if (existing != null) {
                    // Existing user - log in
                    userDao.logoutAll()
                    val updated = existing.copy(isLoggedIn = true, lastLogin = System.currentTimeMillis())
                    userDao.update(updated)
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true, currentUser = updated,
                            loginMessage = "欢迎回来，${updated.nickname.ifBlank { "用户" }}！")
                    }
                } else {
                    // New user - register
                    userDao.logoutAll()
                    val nickname = _uiState.value.nickname.ifBlank { "用户${phone.takeLast(4)}" }
                    val newUser = UserEntity(
                        phone = phone,
                        nickname = nickname,
                        isLoggedIn = true,
                        lastLogin = System.currentTimeMillis()
                    )
                    val id = userDao.insert(newUser)
                    _uiState.update {
                        it.copy(isLoading = false, isLoggedIn = true,
                            currentUser = newUser.copy(id = id),
                            loginMessage = "注册成功！欢迎使用建筑声学计算器")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "登录失败: ${e.message}")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDao.logoutAll()
            _uiState.update { LoginUiState() }
        }
    }
}
