package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        OtpEntity::class,
        ChatRoomEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun otpDao(): OtpDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fames_deal_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensureAdminExists(database)
                    }
                }
            }
        }

        private suspend fun ensureAdminExists(database: AppDatabase) {
            val userDao = database.userDao()
            val existingAdmin = userDao.getUserByEmail("gertrudefrost16@gmail.com")
            if (existingAdmin == null) {
                userDao.insertUser(
                    UserEntity(
                        id = "admin_root_id",
                        fullName = "Administrator",
                        email = "gertrudefrost16@gmail.com",
                        password = "Admin@175757",
                        status = "APPROVED",
                        registeredAt = System.currentTimeMillis() - 86400000L * 30,
                        isAdmin = true,
                        avatarColorHex = "#D4AF37"
                    )
                )
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userDao = database.userDao()
            val chatDao = database.chatDao()

            // 1. Designated Admin
            userDao.insertUser(
                UserEntity(
                    id = "admin_root_id",
                    fullName = "Administrator",
                    email = "gertrudefrost16@gmail.com",
                    password = "Admin@175757",
                    status = "APPROVED",
                    registeredAt = System.currentTimeMillis() - 86400000L * 30,
                    isAdmin = true,
                    avatarColorHex = "#D4AF37"
                )
            )

            // 2. Sample Approved Community Friends
            val sarah = UserEntity(
                id = "user_sarah_02",
                fullName = "Sarah Jenkins",
                email = "sarah.j@famesdeal.net",
                password = "Password123!",
                status = "APPROVED",
                registeredAt = System.currentTimeMillis() - 86400000L * 7,
                isAdmin = false,
                avatarColorHex = "#1E3E62"
            )
            val alex = UserEntity(
                id = "user_alex_03",
                fullName = "Alex Rivera",
                email = "alex.r@famesdeal.net",
                password = "Password123!",
                status = "APPROVED",
                registeredAt = System.currentTimeMillis() - 86400000L * 5,
                isAdmin = false,
                avatarColorHex = "#D4AF37"
            )
            val david = UserEntity(
                id = "user_david_04",
                fullName = "David Chen",
                email = "david.c@famesdeal.net",
                password = "Password123!",
                status = "APPROVED",
                registeredAt = System.currentTimeMillis() - 86400000L * 3,
                isAdmin = false,
                avatarColorHex = "#0B192C"
            )

            // Sample pending registration for admin to review
            val elena = UserEntity(
                id = "user_elena_05",
                fullName = "Elena Rostova",
                email = "elena.rostova@famesdeal.net",
                password = "Password123!",
                status = "PENDING_APPROVAL",
                registeredAt = System.currentTimeMillis() - 3600000L * 2,
                isAdmin = false,
                avatarColorHex = "#B8860B"
            )

            userDao.insertUser(sarah)
            userDao.insertUser(alex)
            userDao.insertUser(david)
            userDao.insertUser(elena)

            // 3. Pre-seed Community Group Chat Room
            val groupRoomId = "group_fames_deal_vip"
            val now = System.currentTimeMillis()

            val groupRoom = ChatRoomEntity(
                id = groupRoomId,
                title = "Fames Deal VIP Lounge",
                isGroup = true,
                createdBy = sarah.id,
                createdAt = now - 7200000L,
                lastMessageText = "Welcome to Fames Deal! 24/7 lifetime secure messaging.",
                lastMessageTimestamp = now - 1800000L,
                memberIds = "${sarah.id},${alex.id},${david.id}"
            )
            chatDao.insertRoom(groupRoom)

            chatDao.insertMessage(
                MessageEntity(
                    id = "msg_seed_1",
                    roomId = groupRoomId,
                    senderId = sarah.id,
                    senderName = sarah.fullName,
                    senderEmail = sarah.email,
                    content = "Hey everyone! Welcome to Fames Deal VIP Lounge.",
                    timestamp = now - 7000000L,
                    status = "READ"
                )
            )
            chatDao.insertMessage(
                MessageEntity(
                    id = "msg_seed_2",
                    roomId = groupRoomId,
                    senderId = alex.id,
                    senderName = alex.fullName,
                    senderEmail = alex.email,
                    content = "Glad to be here! The navy and gold theme is so sleek.",
                    timestamp = now - 5400000L,
                    status = "READ"
                )
            )
            chatDao.insertMessage(
                MessageEntity(
                    id = "msg_seed_3",
                    roomId = groupRoomId,
                    senderId = david.id,
                    senderName = david.fullName,
                    senderEmail = david.email,
                    content = "Welcome to Fames Deal! 24/7 lifetime secure messaging.",
                    timestamp = now - 1800000L,
                    status = "READ"
                )
            )

            // 4. Pre-seed Direct Chat between Sarah & Alex
            val directRoomId = "direct_${sarah.id}_${alex.id}"
            val directRoom = ChatRoomEntity(
                id = directRoomId,
                title = "Sarah Jenkins",
                isGroup = false,
                createdBy = sarah.id,
                createdAt = now - 14400000L,
                lastMessageText = "Are you ready for the partnership review tomorrow?",
                lastMessageTimestamp = now - 3600000L,
                memberIds = "${sarah.id},${alex.id}"
            )
            chatDao.insertRoom(directRoom)

            chatDao.insertMessage(
                MessageEntity(
                    id = "msg_seed_direct_1",
                    roomId = directRoomId,
                    senderId = alex.id,
                    senderName = alex.fullName,
                    senderEmail = alex.email,
                    content = "Hi Sarah, did you get the deal files?",
                    timestamp = now - 12000000L,
                    status = "READ"
                )
            )
            chatDao.insertMessage(
                MessageEntity(
                    id = "msg_seed_direct_2",
                    roomId = directRoomId,
                    senderId = sarah.id,
                    senderName = sarah.fullName,
                    senderEmail = sarah.email,
                    content = "Are you ready for the partnership review tomorrow?",
                    timestamp = now - 3600000L,
                    status = "READ"
                )
            )
        }
    }
}
