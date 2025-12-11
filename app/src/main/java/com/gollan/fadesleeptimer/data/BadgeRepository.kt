package com.gollan.fadesleeptimer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val requiredStreak: Int,
    val color: Color = Color(0xFFF59E0B)
)

object BadgeRepository {
    val badges = listOf(
        Badge(
            id = "streak_3",
            name = "Consistency Starter",
            description = "Hit your bedtime 3 nights in a row",
            icon = Icons.Rounded.Star,
            requiredStreak = 3,
            color = Color(0xFFCD7F32) // Bronze
        ),
        Badge(
            id = "streak_7",
            name = "Sleep Week Warrior",
            description = "Hit your bedtime 7 nights in a row",
            icon = Icons.Rounded.LocalFireDepartment,
            requiredStreak = 7,
            color = Color(0xFFC0C0C0) // Silver
        ),
        Badge(
            id = "streak_30",
            name = "Master of Rest",
            description = "Hit your bedtime 30 nights in a row",
            icon = Icons.Rounded.EmojiEvents, // Trophy
            requiredStreak = 30,
            color = Color(0xFFFFD700) // Gold
        ),
        Badge(
            id = "streak_100",
            name = "Legendary Slumber",
            description = "Hit your bedtime 100 nights in a row",
            icon = Icons.Rounded.MilitaryTech, // Medal
            requiredStreak = 100,
            color = Color(0xFFE91E63) // Pink/Epic
        )
    )

    fun getBadge(id: String): Badge? = badges.find { it.id == id }
}
