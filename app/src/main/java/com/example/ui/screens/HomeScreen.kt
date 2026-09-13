package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatRoomEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MessengerViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val pendingUsers by viewModel.pendingUsers.collectAsState()
    val activeChatRoom by viewModel.activeChatRoom.collectAsState()
    val isGhostMode by viewModel.isGhostMode.collectAsState()
    val showCreateGroup by viewModel.showCreateGroupDialog.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // If a chat room is open, render ChatScreen
    if (activeChatRoom != null) {
        ChatScreen(
            viewModel = viewModel,
            room = activeChatRoom!!,
            isGhostMode = isGhostMode,
            onBack = { viewModel.closeChat() }
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Navy900)
                                .border(1.5.dp, Gold500, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.fd_logo),
                                contentDescription = "Fames Deal Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FAMES DEAL",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Gold400,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                )
                            }
                            Text(
                                text = "24/7 Lifetime-Free Secure Messenger",
                                fontSize = 10.sp,
                                color = PureWhite.copy(alpha = 0.75f)
                            )
                        }
                    }
                },
                actions = {
                    if (currentUser?.isAdmin == true) {
                        IconButton(
                            onClick = { viewModel.setTab(MainTab.ADMIN_CONSOLE) },
                            modifier = Modifier.testTag("admin_header_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (pendingUsers.isNotEmpty()) {
                                        Badge(containerColor = Gold500, contentColor = Navy900) {
                                            Text(pendingUsers.size.toString(), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Admin Console",
                                    tint = Gold400
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = PureWhite.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy800,
                    titleContentColor = PureWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Navy800,
                contentColor = PureWhite
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Navy900,
                    selectedTextColor = Gold400,
                    indicatorColor = Gold400,
                    unselectedIconColor = PureWhite.copy(alpha = 0.6f),
                    unselectedTextColor = PureWhite.copy(alpha = 0.6f)
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.CHATS,
                    onClick = { viewModel.setTab(MainTab.CHATS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats") },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("tab_chats")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.GROUPS,
                    onClick = { viewModel.setTab(MainTab.GROUPS) },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                    label = { Text("Groups", fontSize = 11.sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("tab_groups")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.FRIENDS,
                    onClick = { viewModel.setTab(MainTab.FRIENDS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Friends") },
                    label = { Text("Friends", fontSize = 11.sp) },
                    colors = itemColors,
                    modifier = Modifier.testTag("tab_friends")
                )

                // Admin Console Tab (Only visible to admin)
                if (currentUser?.isAdmin == true) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.ADMIN_CONSOLE,
                        onClick = { viewModel.setTab(MainTab.ADMIN_CONSOLE) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingUsers.isNotEmpty()) {
                                        Badge(containerColor = Gold500, contentColor = Navy900) {
                                            Text(pendingUsers.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = "Admin")
                            }
                        },
                        label = { Text("Admin", fontSize = 11.sp) },
                        colors = itemColors,
                        modifier = Modifier.testTag("tab_admin")
                    )
                }
            }
        },
        floatingActionButton = {
            when (currentTab) {
                MainTab.GROUPS -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openCreateGroupDialog() },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Create Group") },
                        text = { Text("Create Group", fontWeight = FontWeight.Bold) },
                        containerColor = Gold500,
                        contentColor = Navy900,
                        modifier = Modifier.testTag("create_group_fab")
                    )
                }
                MainTab.CHATS -> {
                    FloatingActionButton(
                        onClick = { viewModel.setTab(MainTab.FRIENDS) },
                        containerColor = Gold500,
                        contentColor = Navy900,
                        modifier = Modifier.testTag("start_chat_fab")
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Start Chat")
                    }
                }
                else -> Unit
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.CHATS -> ChatsTab(viewModel = viewModel)
                MainTab.GROUPS -> GroupsTab(viewModel = viewModel)
                MainTab.FRIENDS -> FriendsTab(viewModel = viewModel)
                MainTab.ADMIN_CONSOLE -> AdminConsoleScreen(viewModel = viewModel)
            }
        }
    }

    if (showCreateGroup) {
        CreateGroupDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeCreateGroupDialog() }
        )
    }
}

@Composable
fun ChatsTab(viewModel: MessengerViewModel) {
    val rooms by viewModel.chatRooms.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    // Normal users see rooms they are part of; Admin sees all
    val visibleRooms = remember(rooms, currentUser) {
        if (currentUser?.isAdmin == true) {
            rooms
        } else {
            rooms.filter { room ->
                currentUser != null && room.memberIds.split(",").contains(currentUser!!.id)
            }
        }
    }

    if (visibleRooms.isEmpty()) {
        EmptyStateCard(
            icon = Icons.AutoMirrored.Filled.Chat,
            title = "No Conversations Yet",
            description = "Start a 1-on-1 private chat with a friend or create a group room to begin 24/7 instant messaging."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Navy50)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    color = Navy900,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = Gold400,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "24/7 Encrypted Network",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold400
                            )
                            Text(
                                text = "Lifetime-free instant direct & group messaging.",
                                fontSize = 11.sp,
                                color = PureWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(visibleRooms) { room ->
                RoomListItem(
                    room = room,
                    timeString = if (room.lastMessageTimestamp > 0) dateFormat.format(Date(room.lastMessageTimestamp)) else "",
                    onClick = { viewModel.openChat(room, ghostMode = false) }
                )
            }
        }
    }
}

@Composable
fun GroupsTab(viewModel: MessengerViewModel) {
    val rooms by viewModel.chatRooms.collectAsState()
    val groupRooms = remember(rooms) { rooms.filter { it.isGroup } }

    if (groupRooms.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.Group,
            title = "No Group Rooms",
            description = "Create a group chat room to collaborate with multiple members at once."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Navy50)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(groupRooms) { room ->
                val memberCount = room.memberIds.split(",").filter { it.isNotBlank() }.size
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openChat(room, ghostMode = false) }
                        .testTag("group_item_${room.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Navy800)
                                .border(1.dp, Gold500, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Group",
                                tint = Gold400,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = room.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Gold500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "$memberCount members",
                                        fontSize = 10.sp,
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = if (room.lastMessageText.isNotEmpty()) room.lastMessageText else "No messages yet",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsTab(viewModel: MessengerViewModel) {
    val approvedUsers by viewModel.approvedNormalUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Exclude current user from the friends list
    // Note: ApprovedNormalUsersDao already excludes admin email completely!
    // ("No one will be able to see or find out the admin email.")
    val friendList = remember(approvedUsers, currentUser, searchQuery) {
        approvedUsers
            .filter { it.id != currentUser?.id }
            .filter {
                if (searchQuery.isBlank()) true
                else it.fullName.contains(searchQuery, ignoreCase = true)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy50)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search friends by name...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Gold500)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold500,
                unfocusedBorderColor = Navy700.copy(alpha = 0.3f),
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("friend_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (friendList.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Person,
                title = "No Friends Found",
                description = if (searchQuery.isNotBlank()) "No approved members match \"$searchQuery\"." else "Once users are approved by admin, they will appear here to start 1-on-1 chats."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(friendList) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.openDirectChatWithUser(user)
                            }
                            .testTag("friend_item_${user.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Navy800)
                            ) {
                                Text(
                                    text = user.fullName.take(1).uppercase(),
                                    color = Gold400,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = "Tap to message directly",
                                    fontSize = 11.sp,
                                    color = Gold600
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Direct Chat",
                                tint = Gold500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomListItem(
    room: ChatRoomEntity,
    timeString: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("room_item_${room.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Navy800)
                    .border(1.dp, Gold500, CircleShape)
            ) {
                Icon(
                    imageVector = if (room.isGroup) Icons.Default.Group else Icons.Default.Person,
                    contentDescription = "Room Avatar",
                    tint = Gold400,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        maxLines = 1
                    )

                    if (timeString.isNotEmpty()) {
                        Text(
                            text = timeString,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (room.lastMessageText.isNotEmpty()) room.lastMessageText else "Start messaging...",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
