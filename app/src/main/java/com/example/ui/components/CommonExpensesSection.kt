package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.fluidStaggeredEntrance
import com.example.ui.animation.applePressEffect
import com.example.data.model.CommonExpense
import com.example.data.model.ExpenseCategory
import com.example.domain.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonExpensesSection(
    presets: List<CommonExpense>,
    currencySymbol: String,
    onPresetClick: (CommonExpense) -> Unit,
    onAddCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val distinctCategories = remember(presets) {
        presets.map { it.category }.distinct()
    }

    val filteredPresets = remember(presets, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            presets
        } else {
            presets.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("common_expenses_section"),
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
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(28.dp),
                        isDark = isDark
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(16.dp).align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUICK ADD PRESETS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "${filteredPresets.size} templates",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Category Filter Pills (when multiple categories exist)
            if (distinctCategories.size > 1) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All (${presets.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("preset_filter_all"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    items(distinctCategories) { catName ->
                        val cat = ExpenseCategory.fromString(catName)
                        val count = presets.count { it.category.equals(catName, ignoreCase = true) }
                        FilterChip(
                            selected = selectedCategoryFilter.equals(catName, ignoreCase = true),
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter.equals(catName, ignoreCase = true)) null else catName
                            },
                            label = { Text("${cat.icon} ${cat.displayName} ($count)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("preset_filter_${cat.displayName.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                selectedLabelColor = if (isDark) Color.White else cat.color
                            )
                        )
                    }
                }
            }

            // FlowRow of Pill Chips with visual category badges
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredPresets.forEachIndexed { index, preset ->
                    CommonExpenseChip(
                        preset = preset,
                        currencySymbol = currencySymbol,
                        onClick = { onPresetClick(preset) },
                        modifier = Modifier.fluidStaggeredEntrance(index = index.coerceAtMost(8))
                    )
                }

                // Add Custom Preset Pill
                LiquidGlassCard(
                    modifier = Modifier
                        .testTag("add_custom_preset_btn"),
                    shape = RoundedCornerShape(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    tintAlpha = 0.12f,
                    borderWidth = 1.dp,
                    elevation = 1.dp,
                    isDark = isDark,
                    pressScale = 0.93f,
                    onClick = onAddCustomClick
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Preset",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommonExpenseChip(
    preset: CommonExpense,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val category = ExpenseCategory.fromString(preset.category)
    val amountFormatted = CurrencyFormatter.formatCents(preset.defaultAmountCents, currencySymbol)

    LiquidGlassCard(
        modifier = modifier.testTag("preset_chip_${preset.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        tint = category.color,
        tintAlpha = 0.07f,
        borderWidth = 1.dp,
        borderAlpha = 0.75f,
        elevation = 1.5.dp,
        isDark = isDark,
        pressScale = 0.93f,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Visual Category Badge with Emoji and Category Name
            LiquidGlassBadge(
                tint = category.color,
                shape = RoundedCornerShape(8.dp),
                isDark = isDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.icon, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = category.displayName.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = category.color,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = amountFormatted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
