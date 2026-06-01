package com.acoustics.calculator.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.core.utils.AliyunSmsService
import com.acoustics.calculator.data.local.dao.UserDao
import com.acoustics.calculator.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val loginMessage: String? = null,
    val codeSent: String? = null,
    val hasSavedSession: Boolean? = null,
    val currentUser: UserEntity? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 存储真实下发的验证码
    private var actualCode: String? = null

    init {
        checkExistingLogin()
    }

    /**
     * 检查是否有已保存的登录状态
     */
    private fun checkExistingLogin() {
        viewModelScope.launch {
            val existing = userDao.getCurrentUser()
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        currentUser = existing,
                        hasSavedSession = true
                    )
                }
            } else {
                _uiState.update { it.copy(hasSavedSession = false) }
            }
        }
    }

    fun updatePhone(phone: String) {
        val digits = phone.filter { it.isDigit() }.take(11)
        _uiState.update { it.copy(phone = digits, error = null, codeSent = null) }
    }

    /**
     * 发送验证码 —— 真实调用阿里云短信API
     * 配置好 AliyunSmsService 中的密钥后自动生效
     */
    fun sendCode() {
        val phone = _uiState.value.phone
        if (phone.length < 11) {
            _uiState.update { it.copy(error = "请输入完整的11位手机号码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 生成6位随机验证码
            val code = (100000..999999).random().toString()
            actualCode = code

            val result = AliyunSmsService.sendSmsCode(phone, code)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    codeSent = result.message,
                    error = if (!result.success) result.message else null
                )
            }
        }
    }

    /**
     * 验证码登录
     */
    fun loginWithCode(code: String) {
        val phone = _uiState.value.phone

        if (phone.length < 11) {
            _uiState.update { it.copy(error = "请输入完整的11位手机号码") }
            return
        }

        if (code.length < 4) {
            _uiState.update { it.copy(error = "请输入验证码") }
            return
        }

        // 验证码校验
        if (actualCode != null && code != actualCode) {
            _uiState.update { it.copy(error = "验证码错误，请重新输入") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, loginMessage = null) }

            try {
                val existing = userDao.findByPhone(phone)
                if (existing != null) {
                    // 已有账号 → 登录
                    userDao.logoutAll()
                    val updated = existing.copy(isLoggedIn = true, lastLogin = System.currentTimeMillis())
                    userDao.update(updated)
                    _uiState.update {
                        it.copy(
                            isLoading = false, isLoggedIn = true, currentUser = updated,
                            loginMessage = "欢迎回来，${updated.nickname}！"
                        )
                    }
                } else {
                    // 新用户 → 注册并自动登录
                    userDao.logoutAll()
                    val nickname = "用户${phone.takeLast(4)}"
                    val newUser = UserEntity(
                        phone = phone,
                        nickname = nickname,
                        isLoggedIn = true,
                        lastLogin = System.currentTimeMillis()
                    )
                    userDao.insert(newUser)
                    _uiState.update {
                        it.copy(
                            isLoading = false, isLoggedIn = true,
                            currentUser = newUser,
                            loginMessage = "注册成功！欢迎使用建筑声学计算器"
                        )
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
            _uiState.update { LoginUiState(hasSavedSession = false) }
        }
    }

    /**
     * 获取当前登录用户信息
     */
    suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()
}
