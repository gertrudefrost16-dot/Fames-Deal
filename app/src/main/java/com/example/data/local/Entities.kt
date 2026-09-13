package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val password: String,
    val status: String, // "APPROVED", "PENDING_APPROVAL", "REJECTED"
    val registeredAt: Long,
    val isAdmin: Boolean = false,
    val avatarColorHex: String = "#D4AF37"
)

@Entity(tableName = "email_otps")
data class OtpEntity(
    @PrimaryKey val email: String,
    val code: String,
    val createdAt: Long,
    val expiresAt: Long
)

@Entity(tableName = "chat_rooms")
data class ChatRoomEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isGroup: Boolean,
    val createdBy: String,
    val createdAt: Long,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = 0L,
    val memberIds: String // comma separated list of user IDs
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderEmail: String,
    val content: String,
    val timestamp: Long,
    val status: String = "DELIVERED" // "SENT", "DELIVERED", "READ"
)
