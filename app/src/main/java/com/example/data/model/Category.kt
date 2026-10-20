package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ExpenseCategory(
    val displayName: String,
    val icon: String,
    val color: Color,
    val keywords: List<String>
) {
    HOUSING(
        displayName = "Housing",
        icon = "🏠",
        color = Color(0xFF3B82F6),
        keywords = listOf("rent", "mortgage", "apartment", "condo", "furniture", "repair", "home", "plumbing", "roof")
    ),
    FOOD(
        displayName = "Food",
        icon = "🍔",
        color = Color(0xFF10B981),
        keywords = listOf("grocery", "groceries", "food", "burger", "pizza", "restaurant", "cafe", "coffee", "starbucks", "dinner", "lunch", "snack", "supermarket", "bakery", "mcdonald", "subway")
    ),
    TRANSPORT(
        displayName = "Transport",
        icon = "🚗",
        color = Color(0xFF8B5CF6),
        keywords = listOf("uber", "lyft", "taxi", "gas", "fuel", "petrol", "metro", "subway", "train", "bus", "flight", "parking", "toll", "car", "service")
    ),
    UTILITIES(
        displayName = "Utilities",
        icon = "💡",
        color = Color(0xFFF59E0B),
        keywords = listOf("electric", "electricity", "power", "water", "internet", "wifi", "utility", "trash", "gas bill", "phone", "cellular", "bill", "broadband")
    ),
    ENTERTAINMENT(
        displayName = "Entertainment",
        icon = "🎬",
        color = Color(0xFFEC4899),
        keywords = listOf("netflix", "spotify", "movie", "cinema", "game", "steam", "concert", "theatre", "disney", "hulu", "bar", "club", "party", "show", "playstation", "xbox")
    ),
    OTHER(
        displayName = "Other",
        icon = "📦",
        color = Color(0xFF6366F1),
        keywords = listOf("misc", "shopping", "clothes", "amazon", "gift", "donation", "health", "doctor", "pharmacy")
    );

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
                ?: entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: OTHER
        }

        fun suggestCategory(description: String): ExpenseCategory {
            val lower = description.trim().lowercase()
            if (lower.isEmpty()) return OTHER
            for (cat in entries) {
                if (cat == OTHER) continue
                for (keyword in cat.keywords) {
                    if (lower.contains(keyword)) {
                        return cat
                    }
                }
            }
            return OTHER
        }
    }
}
