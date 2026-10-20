package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import com.example.ui.animation.AppleMotion
import com.example.ui.animation.applePressEffect
import com.example.ui.animation.fluidExpandVertical
import com.example.ui.animation.fluidShrinkVertical
import com.example.ui.animation.fluidStaggeredEntrance
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassBadge
import com.example.data.model.TodoItem
import com.example.ui.theme.RemainingGreen

@Composable
fun TodoSection(
    todos: List<TodoItem>,
    completedCount: Int,
    totalCount: Int,
    onAddTodo: (String) -> Unit,
    onToggleTodo: (TodoItem) -> Unit,
    onDeleteTodo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTodoText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("todo_section"),
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassBadge(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                        isDark = isDark
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp).align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SHOPPING / NOTES LIST",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }

                LiquidGlassBadge(
                    tint = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    isDark = isDark
                ) {
                    Text(
                        text = "$completedCount/$totalCount DONE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTodoText,
                    onValueChange = { newTodoText = it },
                    placeholder = { Text("Buy milk, pay bills...", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newTodoText.isNotBlank()) {
                                onAddTodo(newTodoText)
                                newTodoText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_todo_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Button(
                    onClick = {
                        if (newTodoText.isNotBlank()) {
                            onAddTodo(newTodoText)
                            newTodoText = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .applePressEffect(scaleDown = 0.90f)
                        .testTag("add_todo_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add todo")
                }
            }

            // Items List
            if (todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes or shopping items.\nAdd items to track your purchases!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    todos.forEachIndexed { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fluidExpandVertical(),
                            exit = fluidShrinkVertical()
                        ) {
                            TodoRowItem(
                                item = item,
                                onToggle = { onToggleTodo(item) },
                                onDelete = { onDeleteTodo(item.id) },
                                modifier = Modifier.fluidStaggeredEntrance(index = index.coerceAtMost(6))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoRowItem(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val checkmarkScale by animateFloatAsState(
        targetValue = if (item.isDone) 1f else 0f,
        animationSpec = AppleMotion.BouncySpring,
        label = "check_scale"
    )

    val boxBgColor by animateColorAsState(
        targetValue = if (item.isDone) RemainingGreen else Color.Transparent,
        label = "check_bg"
    )

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("todo_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        borderWidth = 0.8.dp,
        borderAlpha = 0.7f,
        elevation = 1.dp,
        isDark = isDark,
        pressScale = 0.96f,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Custom Rounded Checkbox with Apple-style bounce
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(boxBgColor)
                        .border(
                            width = 1.5.dp,
                            color = if (item.isDone) RemainingGreen else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checkmarkScale > 0.05f) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(14.dp)
                                .scale(checkmarkScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.text,
                    fontSize = 14.sp,
                    color = if (item.isDone) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = if (item.isDone) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .applePressEffect(scaleDown = 0.85f)
                    .testTag("delete_todo_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete todo",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
