package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatRoomEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserEntity
import com.example.data.repository.MessengerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthScreenState {
    object Login : AuthScreenState()
    object Register : AuthScreenState()
    data class VerifyOtp(
        val fullName: String,
        val email: String,
        val password: String,
        val generatedCode: String,
        val isResending: Boolean = false
    ) : AuthScreenState()
    data class PendingApprovalNotice(val email: String) : AuthScreenState()
}

enum class MainTab {
    CHATS,
    GROUPS,
    FRIENDS,
    ADMIN_CONSOLE
}

class MessengerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = MessengerRepository(database)

    // Current logged-in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Auth screen state
    private val _authScreenState = MutableStateFlow<AuthScreenState>(AuthScreenState.Login)
    val authScreenState: StateFlow<AuthScreenState> = _authScreenState.asStateFlow()

    // Navigation state
    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Active chat state
    private val _activeChatRoom = MutableStateFlow<ChatRoomEntity?>(null)
    val activeChatRoom: StateFlow<ChatRoomEntity?> = _activeChatRoom.asStateFlow()

    private val _isGhostMode = MutableStateFlow(false)
    val isGhostMode: StateFlow<Boolean> = _isGhostMode.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages.asStateFlow()

    private var messagesJob: Job? = null

    // Users and Rooms flows
    val pendingUsers: StateFlow<List<UserEntity>> = repository.getPendingUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNormalUsers: StateFlow<List<UserEntity>> = repository.getAllNormalUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedNormalUsers: StateFlow<List<UserEntity>> = repository.getApprovedNormalUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatRooms: StateFlow<List<ChatRoomEntity>> = repository.getAllRoomsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback notification banner
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Dialogs
    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setAuthScreen(state: AuthScreenState) {
        _authScreenState.value = state
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openCreateGroupDialog() {
        _showCreateGroupDialog.value = true
    }

    fun closeCreateGroupDialog() {
        _showCreateGroupDialog.value = false
    }

    // Auth Actions
    fun login(emailInput: String, passwordInput: String, onComplete: (Boolean, String?) -> Unit) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            onComplete(false, "Please fill in both email and password.")
            return
        }

        viewModelScope.launch {
            val result = repository.login(emailInput, passwordInput)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _currentUser.value = user
                _authScreenState.value = AuthScreenState.Login
                _currentTab.value = if (user?.isAdmin == true) MainTab.ADMIN_CONSOLE else MainTab.CHATS
                onComplete(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Login failed."
                onComplete(false, errorMsg)
            }
        }
    }

    fun startRegistration(fullName: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (fullName.isBlank()) {
            onResult(false, "Full Name is required.")
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            val result = repository.generateOtpForRegistration(email)
            if (result.isSuccess) {
                val code = result.getOrNull() ?: "123456"
                _authScreenState.value = AuthScreenState.VerifyOtp(
                    fullName = fullName.trim(),
                    email = email.trim(),
                    password = password.trim(),
                    generatedCode = code
                )
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Registration error.")
            }
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            val currentVerify = _authScreenState.value as? AuthScreenState.VerifyOtp ?: return@launch
            val result = repository.generateOtpForRegistration(email)
            if (result.isSuccess) {
                val newCode = result.getOrNull() ?: "123456"
                _authScreenState.value = currentVerify.copy(
                    generatedCode = newCode,
                    isResending = false
                )
                showToast("New 6-digit OTP sent to $email: $newCode")
            }
        }
    }

    fun verifyOtp(enteredCode: String, onResult: (Boolean, String?) -> Unit) {
        val currentVerify = _authScreenState.value as? AuthScreenState.VerifyOtp ?: return
        viewModelScope.launch {
            val result = repository.verifyOtpAndRegister(
                fullName = currentVerify.fullName,
                emailInput = currentVerify.email,
                password = currentVerify.password,
                enteredOtp = enteredCode
            )
            if (result.isSuccess) {
                _authScreenState.value = AuthScreenState.PendingApprovalNotice(currentVerify.email)
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Invalid OTP code.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _activeChatRoom.value = null
        _isGhostMode.value = false
        _currentTab.value = MainTab.CHATS
        _authScreenState.value = AuthScreenState.Login
    }

    // Admin Actions
    fun approveUser(user: UserEntity) {
        viewModelScope.launch {
            repository.approveUser(user.id)
            showToast("Approved ${user.fullName}. They can now access Fames Deal.")
        }
    }

    fun rejectUser(user: UserEntity) {
        viewModelScope.launch {
            repository.rejectUser(user.id)
            showToast("Declined registration for ${user.fullName}.")
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user.id)
            showToast("Permanently deleted user ${user.fullName}.")
        }
    }

    // Chat Operations
    fun openChat(room: ChatRoomEntity, ghostMode: Boolean = false) {
        _activeChatRoom.value = room
        _isGhostMode.value = ghostMode

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForRoomFlow(room.id).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun openDirectChatWithUser(otherUser: UserEntity) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val room = repository.getOrCreateDirectRoom(current, otherUser)
            openChat(room, ghostMode = false)
        }
    }

    fun closeChat() {
        messagesJob?.cancel()
        _activeChatRoom.value = null
        _isGhostMode.value = false
        _activeMessages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val current = _currentUser.value ?: return
        val room = _activeChatRoom.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(room, current, text)

            // Responsive simulation: if messaging in community room or with simulated contact,
            // send a responsive friendly reply after 1.5s so the user experiences 24/7 instant messaging
            if (!current.isAdmin && (room.isGroup || room.memberIds.contains("user_sarah_02") || room.memberIds.contains("user_alex_03"))) {
                delay(1500)
                simulateSmartReply(room)
            }
        }
    }

    private suspend fun simulateSmartReply(room: ChatRoomEntity) {
        val replies = listOf(
            "Got your message! Fames Deal encryption is lightning fast.",
            "Thanks for reaching out! Everything is 24/7 synchronized.",
            "Sounds great! Looking forward to connecting further.",
            "Confirmed! I received this instantly on my end."
        )
        val replyText = replies.random()

        val sender = if (room.isGroup) {
            UserEntity(
                id = "user_alex_03",
                fullName = "Alex Rivera",
                email = "alex.r@famesdeal.net",
                password = "",
                status = "APPROVED",
                registeredAt = 0L,
                isAdmin = false
            )
        } else {
            UserEntity(
                id = "user_sarah_02",
                fullName = "Sarah Jenkins",
                email = "sarah.j@famesdeal.net",
                password = "",
                status = "APPROVED",
                registeredAt = 0L,
                isAdmin = false
            )
        }

        repository.sendMessage(room, sender, replyText)
    }

    fun createGroup(title: String, selectedMemberIds: List<String>, onResult: (Boolean, String?) -> Unit) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.createGroupRoom(title, current, selectedMemberIds)
            if (result.isSuccess) {
                closeCreateGroupDialog()
                val newRoom = result.getOrNull()
                if (newRoom != null) {
                    openChat(newRoom, ghostMode = false)
                }
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to create group.")
            }
        }
    }
}
