package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.ui.animation.applePressEffect
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassBadge
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategory
import com.example.domain.CurrencyFormatter
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.RemainingGreen
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

enum class ChartStyleMode {
    PIE,   // Solid filled Pie Chart (Recharts / D3 pie)
    DONUT  // Donut Ring Chart with KPI Center (Recharts / D3 arc with innerRadius)
}

data class PieCategorySlice(
    val category: ExpenseCategory,
    val totalAmountCents: Long,
    val transactionCount: Int,
    val percentage: Float,
    val startAngle: Float,
    val sweepAngle: Float
)

/**
 * Interactive Jetpack Compose Pie & Donut Chart inspired by Recharts and D3.
 * Features:
 * - Seamless toggle between Solid Pie and Donut modes
 * - Interactive slice selection with animated "explode" offset along bisector angle
 * - Recharts-style Tooltip popup showing category details, count, and average spend
 * - Interactive Legend chips with tap-to-focus
 * - Detailed Category Breakdown table with proportional spend bars
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPieChart(
    expenses: List<Expense>,
    currencySymbol: String = "$",
    categoryLimits: Map<String, Long> = emptyMap(),
    modifier: Modifier = Modifier
) {
    var chartMode by remember { mutableStateOf(ChartStyleMode.PIE) }
    var selectedSliceIndex by remember { mutableStateOf<Int?>(null) }
    val totalExpensesCents = expenses.sumOf { it.amountCents }

    // Build slices based on category aggregates
    val slices = remember(expenses, totalExpensesCents) {
        if (totalExpensesCents <= 0L) {
            emptyList()
        } else {
            val grouped = expenses.groupBy { it.category }
            var currentAngle = -90f // Start at 12 o'clock (standard D3 / Recharts top orientation)
            val result = mutableListOf<PieCategorySlice>()

            // Sort categories descending by amount so dominant slices render first
            val sortedEntries = grouped.entries.sortedByDescending { it.value.sumOf { exp -> exp.amountCents } }

            sortedEntries.forEach { (catName, catExpenses) ->
                val catTotal = catExpenses.sumOf { it.amountCents }
                val pct = (catTotal.toFloat() / totalExpensesCents.toFloat())
                val sweep = pct * 360f
                val cat = ExpenseCategory.fromString(catName)
                result.add(
                    PieCategorySlice(
                        category = cat,
                        totalAmountCents = catTotal,
                        transactionCount = catExpenses.size,
                        percentage = pct * 100f,
                        startAngle = currentAngle,
                        sweepAngle = sweep
                    )
                )
                currentAngle += sweep
            }
            result
        }
    }

    // Reset selection if expenses change and selection is out of bounds
    LaunchedEffect(slices.size) {
        if (selectedSliceIndex != null && selectedSliceIndex!! >= slices.size) {
            selectedSliceIndex = null
        }
    }

    // Smooth entry animation for the chart sweep
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(totalExpensesCents, chartMode) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    val isDark = isSystemInDarkTheme()

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_pie_chart_card"),
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp,
        isDark = isDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Recharts-style Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = if (chartMode == ChartStyleMode.PIE) Icons.Default.PieChart else Icons.Default.DonutLarge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "EXPENSES BY CATEGORY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (chartMode == ChartStyleMode.PIE) "D3/Recharts Interactive Pie" else "Donut Ring with Metrics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Pie vs Donut Segmented Switcher
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        // Pie option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (chartMode == ChartStyleMode.PIE) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .applePressEffect(scaleDown = 0.92f)
                                .clickable { chartMode = ChartStyleMode.PIE }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("pie_mode_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🥧 Pie",
                                fontSize = 11.sp,
                                fontWeight = if (chartMode == ChartStyleMode.PIE) FontWeight.Bold else FontWeight.Normal,
                                color = if (chartMode == ChartStyleMode.PIE) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Donut option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (chartMode == ChartStyleMode.DONUT) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .applePressEffect(scaleDown = 0.92f)
                                .clickable { chartMode = ChartStyleMode.DONUT }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("donut_mode_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🍩 Donut",
                                fontSize = 11.sp,
                                fontWeight = if (chartMode == ChartStyleMode.DONUT) FontWeight.Bold else FontWeight.Normal,
                                color = if (chartMode == ChartStyleMode.DONUT) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (slices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📊",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses recorded this month",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Add expenses in the Dashboard to visualize category distribution.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Interactive Chart Canvas Area
                val surfaceBg = MaterialTheme.colorScheme.surface
                val activeIndex = selectedSliceIndex
                val animProgress = animationProgress.value

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(220.dp)
                            .testTag("pie_canvas")
                            .semantics {
                                val topCategories = slices.take(3).joinToString { "${it.category.displayName} (${String.format(Locale.US, "%.1f", it.percentage)}%)" }
                                contentDescription = "Expense breakdown chart with ${slices.size} categories. Total spend: ${CurrencyFormatter.formatCents(totalExpensesCents, currencySymbol)}. Top categories: $topCategories"
                            }
                            .pointerInput(slices, chartMode) {
                                detectTapGestures { offset ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val dx = offset.x - centerX
                                    val dy = offset.y - centerY
                                    val distance = sqrt(dx * dx + dy * dy)
                                    val outerRadius = size.width / 2f
                                    val innerRadius = if (chartMode == ChartStyleMode.DONUT) outerRadius * 0.58f else 0f

                                    if (distance in innerRadius..outerRadius * 1.15f) {
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (angle < 0) angle += 360f
                                        // Align with 12 o'clock (-90 deg offset)
                                        val normalizedAngle = (angle + 90f) % 360f

                                        var accumulated = 0f
                                        var found: Int? = null
                                        for (i in slices.indices) {
                                            val slice = slices[i]
                                            if (normalizedAngle >= accumulated && normalizedAngle < accumulated + slice.sweepAngle) {
                                                found = i
                                                break
                                            }
                                            accumulated += slice.sweepAngle
                                        }

                                        selectedSliceIndex = if (selectedSliceIndex == found) null else found
                                    } else {
                                        selectedSliceIndex = null
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val diameter = min(canvasWidth, canvasHeight)
                        val outerRadius = diameter / 2f
                        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                        val isDonut = chartMode == ChartStyleMode.DONUT
                        val strokeWidth = diameter * 0.22f
                        val arcSize = diameter - (if (isDonut) strokeWidth else 0f)

                        // 1. Draw subtle background track in Donut mode
                        if (isDonut) {
                            val trackTopLeft = Offset((canvasWidth - arcSize) / 2f, (canvasHeight - arcSize) / 2f)
                            drawArc(
                                color = Color.Gray.copy(alpha = 0.12f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = trackTopLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        // 2. Draw Slices with D3 / Recharts-style Explode Effect
                        slices.forEachIndexed { index, slice ->
                            val isSelected = activeIndex == index
                            val sweep = slice.sweepAngle * animProgress
                            if (sweep <= 0.1f) return@forEachIndexed

                            // Calculate explode displacement along the bisector angle
                            val bisectorAngleDeg = slice.startAngle + (slice.sweepAngle / 2f)
                            val bisectorRad = Math.toRadians(bisectorAngleDeg.toDouble())
                            val explodeOffset = if (isSelected) 10.dp.toPx() else 0f
                            val offsetX = (cos(bisectorRad) * explodeOffset).toFloat()
                            val offsetY = (sin(bisectorRad) * explodeOffset).toFloat()

                            val sliceTopLeft = if (isDonut) {
                                Offset(
                                    (canvasWidth - arcSize) / 2f + offsetX,
                                    (canvasHeight - arcSize) / 2f + offsetY
                                )
                            } else {
                                Offset(
                                    (canvasWidth - diameter) / 2f + offsetX,
                                    (canvasHeight - diameter) / 2f + offsetY
                                )
                            }

                            val sliceSize = if (isDonut) Size(arcSize, arcSize) else Size(diameter, diameter)

                            if (isDonut) {
                                val currentStroke = if (isSelected) strokeWidth * 1.18f else strokeWidth
                                val gap = if (slices.size > 1) 2.5f else 0f
                                val actualSweep = (sweep - gap).coerceAtLeast(0.5f)

                                drawArc(
                                    color = slice.category.color,
                                    startAngle = slice.startAngle + (gap / 2f),
                                    sweepAngle = actualSweep,
                                    useCenter = false,
                                    topLeft = sliceTopLeft,
                                    size = sliceSize,
                                    style = Stroke(
                                        width = currentStroke,
                                        cap = StrokeCap.Round
                                    )
                                )
                            } else {
                                // Solid Pie Slice (Recharts standard)
                                val gap = if (slices.size > 1) 1.5f else 0f
                                val actualSweep = (sweep - gap).coerceAtLeast(0.5f)

                                // Draw sector wedge
                                drawArc(
                                    color = slice.category.color,
                                    startAngle = slice.startAngle + (gap / 2f),
                                    sweepAngle = actualSweep,
                                    useCenter = true,
                                    topLeft = sliceTopLeft,
                                    size = sliceSize,
                                    style = Fill
                                )

                                // Crisp separation stroke like D3 padAngle border
                                drawArc(
                                    color = surfaceBg,
                                    startAngle = slice.startAngle + (gap / 2f),
                                    sweepAngle = actualSweep,
                                    useCenter = true,
                                    topLeft = sliceTopLeft,
                                    size = sliceSize,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Highlight border if selected
                                if (isSelected) {
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.8f),
                                        startAngle = slice.startAngle + (gap / 2f),
                                        sweepAngle = actualSweep,
                                        useCenter = true,
                                        topLeft = sliceTopLeft,
                                        size = sliceSize,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    // Center Content for Donut Mode
                    if (chartMode == ChartStyleMode.DONUT) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            val activeSlice = activeIndex?.let { slices.getOrNull(it) }

                            if (activeSlice != null) {
                                val amountStr = CurrencyFormatter.formatCents(activeSlice.totalAmountCents, currencySymbol)
                                val amountSize = when {
                                    amountStr.length > 13 -> 12.sp
                                    amountStr.length > 10 -> 13.sp
                                    amountStr.length > 7 -> 14.sp
                                    else -> 16.sp
                                }

                                Text(
                                    text = "${activeSlice.category.icon} ${activeSlice.category.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeSlice.category.color,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = amountStr,
                                    fontSize = amountSize,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", activeSlice.percentage)}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            } else {
                                val totalStr = CurrencyFormatter.formatCents(totalExpensesCents, currencySymbol)
                                val totalSize = when {
                                    totalStr.length > 13 -> 12.sp
                                    totalStr.length > 10 -> 14.sp
                                    totalStr.length > 7 -> 15.sp
                                    else -> 17.sp
                                }

                                Text(
                                    text = "TOTAL SPENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = totalStr,
                                    fontSize = totalSize,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${slices.size} Categories",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 3. Recharts-Style Active Tooltip Card
                val activeSlice = activeIndex?.let { slices.getOrNull(it) }
                AnimatedVisibility(
                    visible = activeSlice != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (activeSlice != null) {
                        val cap = categoryLimits[activeSlice.category.displayName]
                        val isOverCap = cap != null && activeSlice.totalAmountCents > cap
                        val avgSpend = activeSlice.totalAmountCents / activeSlice.transactionCount.coerceAtLeast(1)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recharts_tooltip_card"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(activeSlice.category.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${activeSlice.category.icon} ${activeSlice.category.displayName}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = { selectedSliceIndex = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close tooltip",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Category Total",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatCents(activeSlice.totalAmountCents, currencySymbol),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = activeSlice.category.color
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Share of Budget",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", activeSlice.percentage)}%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${activeSlice.transactionCount} transactions • Avg ${CurrencyFormatter.formatCents(avgSpend, currencySymbol)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (cap != null) {
                                        if (isOverCap) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = ExpenseRed,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Over cap by ${CurrencyFormatter.formatCents(activeSlice.totalAmountCents - cap, currencySymbol)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ExpenseRed
                                                )
                                            }
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = RemainingGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Under cap (Limit: ${CurrencyFormatter.formatCents(cap, currencySymbol)})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = RemainingGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Interactive Recharts Legend Chips
                Text(
                    text = "CATEGORIES (TAP TO HIGHLIGHT)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEachIndexed { index, slice ->
                        val isSelected = activeIndex == index
                        val cap = categoryLimits[slice.category.displayName]
                        val isOverCap = cap != null && slice.totalAmountCents >= cap

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .applePressEffect(scaleDown = 0.94f)
                                .clickable {
                                    selectedSliceIndex = if (activeIndex == index) null else index
                                }
                                .testTag("pie_legend_${slice.category.displayName.lowercase()}"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                slice.category.color.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(slice.category.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slice.category.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", slice.percentage)}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = slice.category.color
                                )

                                if (isOverCap) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Over Limit",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Category Breakdown Progress Bars (Recharts Data Table view)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CATEGORY SPENDING BREAKDOWN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEachIndexed { rank, slice ->
                        val isSelected = activeIndex == rank
                        val cap = categoryLimits[slice.category.displayName]
                        val progressFraction = (slice.percentage / 100f).coerceIn(0f, 1f)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedSliceIndex = if (activeIndex == rank) null else rank
                                }
                                .background(
                                    if (isSelected) slice.category.color.copy(alpha = 0.12f) else Color.Transparent
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${rank + 1}.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(slice.category.icon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = slice.category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${slice.transactionCount})",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyFormatter.formatCents(slice.totalAmountCents, currencySymbol),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", slice.percentage)}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = slice.category.color,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            // Horizontal proportion bar
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = slice.category.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
