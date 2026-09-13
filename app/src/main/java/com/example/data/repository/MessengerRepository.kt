package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatRoomEntity
import com.example.data.local.MessageEntity
import com.example.data.local.OtpEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class MessengerRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val otpDao = database.otpDao()
    private val chatDao = database.chatDao()

    companion object {
        const val ADMIN_EMAIL = "gertrudefrost16@gmail.com"
        const val ADMIN_PASS = "Admin@175757"
    }

    suspend fun login(emailInput: String, passwordInput: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            val email = emailInput.trim().lowercase()
            val password = passwordInput.trim()

            // Special check for designated admin
            if (email == ADMIN_EMAIL.lowercase()) {
                if (password == ADMIN_PASS) {
                    var admin = userDao.getUserByEmail(ADMIN_EMAIL)
                    if (admin == null) {
                        admin = UserEntity(
                            id = "admin_root_id",
                            fullName = "Administrator",
                            email = ADMIN_EMAIL,
                            password = ADMIN_PASS,
                            status = "APPROVED",
                            registeredAt = System.currentTimeMillis(),
                            isAdmin = true,
                            avatarColorHex = "#D4AF37"
                        )
                        userDao.insertUser(admin)
                    }
                    return@withContext Result.success(admin)
                } else {
                    return@withContext Result.failure(Exception("Invalid password."))
                }
            }

            // Normal user login
            val user = userDao.getUserByEmail(email)
                ?: return@withContext Result.failure(Exception("Account not found. Please register first."))

            if (user.password != password) {
                return@withContext Result.failure(Exception("Incorrect password. Please try again."))
            }

            when (user.status) {
                "PENDING_APPROVAL" -> {
                    Result.failure(Exception("Your account registration is pending review by the administrator. You will be able to log in once approved."))
                }
                "REJECTED" -> {
                    Result.failure(Exception("Your account access request was declined."))
                }
                "APPROVED" -> {
                    Result.success(user)
                }
                else -> {
                    Result.failure(Exception("Account status is invalid: ${user.status}"))
                }
            }
        }

    suspend fun generateOtpForRegistration(emailInput: String): Result<String> =
        withContext(Dispatchers.IO) {
            val email = emailInput.trim().lowercase()
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with this email already exists."))
            }

            // Generate 6 digit OTP
            val otpCode = (100000..999999).random().toString()
            val now = System.currentTimeMillis()
            val otp = OtpEntity(
                email = email,
                code = otpCode,
                createdAt = now,
                expiresAt = now + (15 * 60 * 1000L) // 15 mins
            )
            otpDao.insertOtp(otp)
            Result.success(otpCode)
        }

    suspend fun verifyOtpAndRegister(
        fullName: String,
        emailInput: String,
        password: String,
        enteredOtp: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val email = emailInput.trim().lowercase()
        val storedOtp = otpDao.getOtpForEmail(email)
            ?: return@withContext Result.failure(Exception("OTP expired or not found. Please request a new code."))

        if (storedOtp.code != enteredOtp.trim()) {
            return@withContext Result.failure(Exception("Invalid 6-digit OTP code. Please check and try again."))
        }

        if (System.currentTimeMillis() > storedOtp.expiresAt) {
            otpDao.deleteOtpForEmail(email)
            return@withContext Result.failure(Exception("This OTP has expired. Please request a new code."))
        }

        // OTP Verified! Clean up OTP record
        otpDao.deleteOtpForEmail(email)

        // Create user with PENDING_APPROVAL status
        val newUser = UserEntity(
            id = "user_" + UUID.randomUUID().toString().take(8),
            fullName = fullName.trim(),
            email = email,
            password = password.trim(),
            status = "PENDING_APPROVAL",
            registeredAt = System.currentTimeMillis(),
            isAdmin = false,
            avatarColorHex = getRandomAvatarColor()
        )
        userDao.insertUser(newUser)
        Result.success(newUser)
    }

    // Admin Capabilities
    fun getPendingUsersFlow(): Flow<List<UserEntity>> = userDao.getPendingUsersFlow()

    fun getAllNormalUsersFlow(): Flow<List<UserEntity>> = userDao.getAllNormalUsersFlow()

    fun getApprovedNormalUsersFlow(): Flow<List<UserEntity>> = userDao.getApprovedNormalUsersFlow()

    suspend fun approveUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, "APPROVED")
    }

    suspend fun rejectUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.updateUserStatus(userId, "REJECTED")
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        // Permanently deletes user
        userDao.deleteUserById(userId)
    }

    // Chat Capabilities
    fun getAllRoomsFlow(): Flow<List<ChatRoomEntity>> = chatDao.getAllRoomsFlow()

    fun getMessagesForRoomFlow(roomId: String): Flow<List<MessageEntity>> =
        chatDao.getMessagesForRoomFlow(roomId)

    suspend fun getOrCreateDirectRoom(currentUser: UserEntity, otherUser: UserEntity): ChatRoomEntity =
        withContext(Dispatchers.IO) {
            val user1 = minOf(currentUser.id, otherUser.id)
            val user2 = maxOf(currentUser.id, otherUser.id)
            val roomId = "direct_${user1}_$user2"

            val existing = chatDao.getRoomById(roomId)
            if (existing != null) {
                return@withContext existing
            }

            val newRoom = ChatRoomEntity(
                id = roomId,
                title = otherUser.fullName,
                isGroup = false,
                createdBy = currentUser.id,
                createdAt = System.currentTimeMillis(),
                lastMessageText = "Started a conversation",
                lastMessageTimestamp = System.currentTimeMillis(),
                memberIds = "${currentUser.id},${otherUser.id}"
            )
            chatDao.insertRoom(newRoom)
            newRoom
        }

    suspend fun createGroupRoom(
        title: String,
        creator: UserEntity,
        selectedMemberIds: List<String>
    ): Result<ChatRoomEntity> = withContext(Dispatchers.IO) {
        if (title.isBlank()) {
            return@withContext Result.failure(Exception("Please provide a group name."))
        }
        val members = (selectedMemberIds + creator.id).distinct()
        if (members.size < 2) {
            return@withContext Result.failure(Exception("Please select at least 1 member to create a group."))
        }

        val roomId = "group_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()
        val room = ChatRoomEntity(
            id = roomId,
            title = title.trim(),
            isGroup = true,
            createdBy = creator.id,
            createdAt = now,
            lastMessageText = "Group created by ${creator.fullName}",
            lastMessageTimestamp = now,
            memberIds = members.joinToString(",")
        )
        chatDao.insertRoom(room)

        // Insert initial system message
        chatDao.insertMessage(
            MessageEntity(
                id = "sys_" + UUID.randomUUID().toString().take(8),
                roomId = roomId,
                senderId = "system",
                senderName = "System",
                senderEmail = "",
                content = "${creator.fullName} created the group \"${title.trim()}\".",
                timestamp = now,
                status = "READ"
            )
        )

        Result.success(room)
    }

    suspend fun sendMessage(
        room: ChatRoomEntity,
        sender: UserEntity,
        content: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return@withContext Result.failure(Exception("Message cannot be empty."))

        val now = System.currentTimeMillis()
        val message = MessageEntity(
            id = "msg_" + UUID.randomUUID().toString(),
            roomId = room.id,
            senderId = sender.id,
            senderName = sender.fullName,
            senderEmail = sender.email,
            content = trimmed,
            timestamp = now,
            status = "DELIVERED"
        )
        chatDao.insertMessage(message)
        chatDao.updateRoomLastMessage(room.id, trimmed, now)
        Result.success(Unit)
    }

    suspend fun getUserById(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    private fun getRandomAvatarColor(): String {
        val colors = listOf("#0B192C", "#1E3E62", "#D4AF37", "#B8860B", "#2563EB", "#059669")
        return colors.random()
    }
}
