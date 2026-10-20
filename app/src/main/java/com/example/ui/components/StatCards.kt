package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.example.ui.animation.AppleMotion
import com.example.ui.animation.applePressEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.domain.BudgetHealth
import com.example.domain.CurrencyFormatter
import com.example.domain.PaceStatus
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeBlue
import com.example.ui.theme.ProgressBarTrack
import com.example.ui.theme.RemainingGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonthSwitcherHeader(
    currentMonthYear: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayMonth = try {
        val parser = SimpleDateFormat("yyyy-MM", Locale.US)
        val date = parser.parse(currentMonthYear) ?: Date()
        SimpleDateFormat("MMMM yyyy", Locale.US).format(date)
    } catch (e: Exception) {
        currentMonthYear
    }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevMonth,
                modifier = Modifier
                    .applePressEffect(scaleDown = 0.85f)
                    .testTag("prev_month_btn")
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .applePressEffect(scaleDown = 0.85f)
                    .testTag("next_month_btn")
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
    }
}

@Composable
fun StatCards(
    monthlyIncomeCents: Long,
    totalExpensesCents: Long,
    remainingBudgetCents: Long,
    percentageUsed: Float,
    currencySymbol: String,
    health: BudgetHealth?,
    onEditIncomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Responsive KPI Cards (adapts to narrow phones, split-screen, and tablets)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompactScreen = maxWidth < 350.dp
            val isDeficit = remainingBudgetCents < 0
            val remainingColor = if (isDeficit) ExpenseRed else RemainingGreen

            if (isCompactScreen) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatKpiCard(
                            title = "INCOME",
                            amountStr = CurrencyFormatter.formatCents(monthlyIncomeCents, currencySymbol),
                            accentColor = IncomeBlue,
                            icon = Icons.Default.AccountBalanceWallet,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditIncomeClick() }
                                .testTag("kpi_card_income"),
                            hasEditIndicator = true
                        )

                        StatKpiCard(
                            title = "EXPENSES",
                            amountStr = CurrencyFormatter.formatCents(totalExpensesCents, currencySymbol),
                            accentColor = ExpenseRed,
                            icon = Icons.Default.Receipt,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_card_expenses")
                        )
                    }

                    StatKpiCard(
                        title = "REMAINING BUDGET",
                        amountStr = CurrencyFormatter.formatCents(remainingBudgetCents, currencySymbol),
                        accentColor = remainingColor,
                        icon = Icons.Default.PieChart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kpi_card_remaining")
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatKpiCard(
                        title = "INCOME",
                        amountStr = CurrencyFormatter.formatCents(monthlyIncomeCents, currencySymbol),
                        accentColor = IncomeBlue,
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onEditIncomeClick() }
                            .testTag("kpi_card_income"),
                        hasEditIndicator = true
                    )

                    StatKpiCard(
                        title = "EXPENSES",
                        amountStr = CurrencyFormatter.formatCents(totalExpensesCents, currencySymbol),
                        accentColor = ExpenseRed,
                        icon = Icons.Default.Receipt,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_expenses")
                    )

                    StatKpiCard(
                        title = "REMAINING",
                        amountStr = CurrencyFormatter.formatCents(remainingBudgetCents, currencySymbol),
                        accentColor = remainingColor,
                        icon = Icons.Default.PieChart,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_card_remaining")
                    )
                }
            }
        }

        // Budget Pace & Health Banner
        if (health != null) {
            BudgetHealthBanner(health = health, currencySymbol = currencySymbol)
        }

        // Budget Progress Bar Card
        val isDark = isSystemInDarkTheme()
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("budget_progress_card"),
            shape = RoundedCornerShape(18.dp),
            elevation = 3.dp,
            isDark = isDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BUDGET USED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    AnimatedContent(targetState = percentageUsed, label = "pct_anim") { pct ->
                        Text(
                            text = "${String.format(Locale.US, "%.1f", pct)}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                pct > 100f -> ExpenseRed
                                pct > 85f -> Color(0xFFF59E0B)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = (percentageUsed / 100f).coerceIn(0f, 1f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "budget_progress"
                )

                val progressColor by animateColorAsState(
                    targetValue = when {
                        percentageUsed > 100f -> ExpenseRed
                        percentageUsed > 85f -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    label = "progress_color"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("budget_progress_indicator"),
                    color = progressColor,
                    trackColor = ProgressBarTrack
                )
            }
        }
    }
}

@Composable
fun BudgetHealthBanner(
    health: BudgetHealth,
    currencySymbol: String
) {
    val isDark = isSystemInDarkTheme()
    val (iconColor, statusIcon) = when (health.paceStatus) {
        PaceStatus.DANGER_PACE -> Pair(ExpenseRed, Icons.Default.Warning)
        PaceStatus.WARNING_PACE -> Pair(Color(0xFFF59E0B), Icons.Default.Warning)
        PaceStatus.ON_TRACK, PaceStatus.UNDER_PACING -> Pair(RemainingGreen, Icons.Default.Info)
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_health_banner"),
        shape = RoundedCornerShape(14.dp),
        tint = iconColor,
        tintAlpha = 0.14f,
        elevation = 2.dp,
        isDark = isDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassBadge(
                tint = iconColor,
                modifier = Modifier.size(28.dp),
                isDark = isDark
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp).align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = health.headlineMessage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${health.detailMessage} (~${CurrencyFormatter.formatCents(health.dailyBudgetRemainingCents, currencySymbol)}/day)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatKpiCard(
    title: String,
    amountStr: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    hasEditIndicator: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    LiquidGlassCard(
        modifier = modifier.applePressEffect(scaleDown = 0.95f),
        shape = RoundedCornerShape(18.dp),
        tint = accentColor,
        tintAlpha = 0.10f,
        elevation = 3.dp,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassBadge(
                    tint = accentColor,
                    modifier = Modifier.size(28.dp),
                    isDark = isDark
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp).align(Alignment.Center)
                    )
                }

                if (hasEditIndicator) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit income",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Scalable dynamic font size based on text length to prevent overflow on any phone
            val amountFontSize = when {
                amountStr.length > 13 -> 12.sp
                amountStr.length > 10 -> 13.sp
                amountStr.length > 7 -> 14.5.sp
                else -> 16.sp
            }

            AnimatedContent(
                targetState = amountStr,
                transitionSpec = {
                    (slideInVertically(animationSpec = AppleMotion.OffsetSpring) { height -> height / 2 } + fadeIn())
                        .togetherWith(slideOutVertically(animationSpec = AppleMotion.OffsetSpring) { height -> -height / 2 } + fadeOut())
                },
                label = "amount_anim"
            ) { text ->
                Text(
                    text = text,
                    fontSize = amountFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
