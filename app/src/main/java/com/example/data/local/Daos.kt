package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE status = 'APPROVED' AND isAdmin = 0 ORDER BY fullName ASC")
    fun getApprovedNormalUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE status = 'PENDING_APPROVAL' ORDER BY registeredAt DESC")
    fun getPendingUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isAdmin = 0 ORDER BY registeredAt DESC")
    fun getAllNormalUsersFlow(): Flow<List<UserEntity>>

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface OtpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtp(otp: OtpEntity)

    @Query("SELECT * FROM email_otps WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getOtpForEmail(email: String): OtpEntity?

    @Query("DELETE FROM email_otps WHERE LOWER(email) = LOWER(:email)")
    suspend fun deleteOtpForEmail(email: String)
}

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: ChatRoomEntity)

    @Update
    suspend fun updateRoom(room: ChatRoomEntity)

    @Query("SELECT * FROM chat_rooms ORDER BY lastMessageTimestamp DESC")
    fun getAllRoomsFlow(): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE id = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: String): ChatRoomEntity?

    @Query("DELETE FROM chat_rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoomFlow(roomId: String): Flow<List<MessageEntity>>

    @Query("UPDATE chat_rooms SET lastMessageText = :lastText, lastMessageTimestamp = :time WHERE id = :roomId")
    suspend fun updateRoomLastMessage(roomId: String, lastText: String, time: Long)

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    suspend fun deleteMessagesForRoom(roomId: String)

    @Query("SELECT COUNT(*) FROM chat_rooms")
    suspend fun getRoomCount(): Int
}
