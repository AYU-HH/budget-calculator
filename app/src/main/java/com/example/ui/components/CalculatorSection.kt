package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalcKeyActionBg
import com.example.ui.theme.CalcKeyClearBg
import com.example.ui.theme.CalcKeyEqualsBg
import com.example.ui.theme.CalcKeyNumberBg
import com.example.ui.theme.CalcKeyOperatorBg
import com.example.ui.theme.CalcScreenBg

@Composable
fun CalculatorSection(
    expression: String,
    display: String,
    onKeyClick: (String) -> Unit,
    onUseInAmountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calculator_section"),
        shape = RoundedCornerShape(24.dp),
        tint = MaterialTheme.colorScheme.primary,
        tintAlpha = 0.05f,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassBadge(
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                    isDark = isDark
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp).align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MATH EXPRESSION CALCULATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            // Screen Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFF1E293B))
                    .border(
                        1.dp,
                        if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFF334155),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("calculator_screen"),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.matchParentSize()
                ) {
                    Text(
                        text = if (expression.isEmpty()) " " else expression,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )

                    AnimatedContent(
                        targetState = display,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { height -> height / 3 })
                                .togetherWith(fadeOut() + slideOutVertically { height -> -height / 3 })
                        },
                        label = "calc_display_anim"
                    ) { currentDisplay ->
                        val displayFontSize = when {
                            currentDisplay.length > 14 -> 16.sp
                            currentDisplay.length > 10 -> 20.sp
                            currentDisplay.length > 7 -> 24.sp
                            else -> 30.sp
                        }

                        Text(
                            text = currentDisplay,
                            fontSize = displayFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Keypad Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: C, (, ), %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcKey("C", CalcKeyClearBg, Color.Black, Modifier.weight(1f)) { onKeyClick("C") }
                    CalcKey("(", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick("(") }
                    CalcKey(")", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick(")") }
                    CalcKey("%", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick("%") }
                    CalcKey("÷", CalcKeyOperatorBg, Color.White, Modifier.weight(1f)) { onKeyClick("÷") }
                }

                // Row 2: 7, 8, 9, ^, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcKey("7", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("7") }
                    CalcKey("8", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("8") }
                    CalcKey("9", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("9") }
                    CalcKey("^", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick("^") }
                    CalcKey("×", CalcKeyOperatorBg, Color.White, Modifier.weight(1f)) { onKeyClick("×") }
                }

                // Row 3: 4, 5, 6, √, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcKey("4", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("4") }
                    CalcKey("5", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("5") }
                    CalcKey("6", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("6") }
                    CalcKey("√", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick("√") }
                    CalcKey("-", CalcKeyOperatorBg, Color.White, Modifier.weight(1f)) { onKeyClick("-") }
                }

                // Row 4: 1, 2, 3, π, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcKey("1", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("1") }
                    CalcKey("2", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("2") }
                    CalcKey("3", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick("3") }
                    CalcKey("π", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick("π") }
                    CalcKey("+", CalcKeyOperatorBg, Color.White, Modifier.weight(1f)) { onKeyClick("+") }
                }

                // Row 5: 0, ., e, =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcKey("0", CalcKeyNumberBg, Color.White, Modifier.weight(1.5f)) { onKeyClick("0") }
                    CalcKey(".", CalcKeyNumberBg, Color.White, Modifier.weight(1f)) { onKeyClick(".") }
                    CalcKey("e", CalcKeyActionBg, Color.Black, Modifier.weight(1f)) { onKeyClick("e") }
                    CalcKey("=", CalcKeyEqualsBg, Color.White, Modifier.weight(1.5f)) { onKeyClick("=") }
                }

                // Bottom Action Row: Backspace + Use in Amount Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("calc_key_backspace"),
                        shape = RoundedCornerShape(12.dp),
                        tint = Color(0xFF475569),
                        tintAlpha = 0.35f,
                        borderWidth = 1.dp,
                        elevation = 1.dp,
                        isDark = isDark,
                        onClick = { onKeyClick("BACK") }
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("calc_use_in_amount_btn"),
                        shape = RoundedCornerShape(12.dp),
                        tint = Color(0xFF7C3AED),
                        tintAlpha = 0.70f,
                        borderWidth = 1.dp,
                        elevation = 2.dp,
                        isDark = isDark,
                        onClick = onUseInAmountClick
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Use in Amount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalcKey(
    text: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        modifier = modifier
            .height(50.dp)
            .testTag("calc_key_$text"),
        shape = RoundedCornerShape(12.dp),
        tint = bg,
        tintAlpha = 0.40f,
        borderWidth = 0.8.dp,
        borderAlpha = 0.8f,
        elevation = 1.dp,
        isDark = isDark,
        pressScale = 0.88f,
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 2) 13.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else textColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
