package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MessengerViewModel

@Composable
fun CreateGroupDialog(
    viewModel: MessengerViewModel,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val approvedUsers by viewModel.approvedNormalUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Filter out current user from selection list
    val selectableUsers = remember(approvedUsers, currentUser) {
        approvedUsers.filter { it.id != currentUser?.id }
    }

    val selectedUserIds = remember { mutableStateListOf<String>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_group_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Gold500.copy(alpha = 0.2f))
                        .border(1.dp, Gold500, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "New Group",
                        tint = Gold500,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Create Group Room",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Name your group and choose members to start chatting:",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = {
                        groupName = it
                        errorMessage = null
                    },
                    label = { Text("Group Name") },
                    placeholder = { Text("e.g. VIP Traders, Strategy Team") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold500,
                        unfocusedBorderColor = Navy700.copy(alpha = 0.4f),
                        focusedLabelColor = Gold500
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Members (${selectedUserIds.size} selected):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy900
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (selectableUsers.isEmpty()) {
                    Text(
                        text = "No other approved members yet. Once approved, members will appear here.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(4.dp)) {
                            items(selectableUsers) { user ->
                                val isSelected = selectedUserIds.contains(user.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) {
                                                selectedUserIds.remove(user.id)
                                            } else {
                                                selectedUserIds.add(user.id)
                                            }
                                            errorMessage = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Navy800)
                                    ) {
                                        Text(
                                            text = user.fullName.take(1).uppercase(),
                                            color = Gold400,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.fullName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Navy900
                                        )
                                    }

                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedUserIds.add(user.id)
                                            } else {
                                                selectedUserIds.remove(user.id)
                                            }
                                            errorMessage = null
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Gold500,
                                            checkmarkColor = Navy900
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = StatusRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isBlank()) {
                        errorMessage = "Please enter a group name."
                        return@Button
                    }
                    if (selectedUserIds.isEmpty()) {
                        errorMessage = "Please select at least 1 member."
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.createGroup(groupName, selectedUserIds.toList()) { success, err ->
                        isSubmitting = false
                        if (!success) errorMessage = err
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold500,
                    contentColor = Navy900
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_create_group_button")
            ) {
                Text(
                    text = "Create Group Room",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cancel_create_group_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
