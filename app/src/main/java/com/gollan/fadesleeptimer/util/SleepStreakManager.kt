package com.gollan.fadesleeptimer.util

import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.data.BadgeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object SleepStreakManager {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    data class StreakResult(
        val settings: AppSettings,
        val newBadgesEarned: List<String>
    )

    fun checkStreak(settings: AppSettings): StreakResult {
        // If consistency tracking is disabled (e.g. bed time not set?), maybe return early?
        // But let's assume if they start timer, we check.

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)
        
        // 1. Check if "Valid Bedtime"
        // Target time today
        val targetBedtime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(settings.bedtimeHour, settings.bedtimeMinute))
        
        // Handle late night timer starts (e.g. 1AM) where target was yesterday 10PM
        // If now is 1AM, and target is 10PM today, diff is ~21 hours. 
        // We want to check against NEAREST target instance.
        
        // Calculate diff in minutes to target
        val diffMinutes = ChronoUnit.MINUTES.between(now, targetBedtime)
        val _absDiff = abs(diffMinutes)
        
        // Let's say +/- 45 mins is "On Time" for consistency
        // Also handle the day wrap case: 11:50 PM vs 00:10 AM
        // Simple logic: Is NOW within 45 mins of Target (Today or Yesterday)?
        
        // This is complex. Simplified logic:
        // Did they start timer between (Bedtime - 45min) and (Bedtime + 45min)?
        
        val isValidTime = isWithinConsistencyWindow(now, settings.bedtimeHour, settings.bedtimeMinute)
        
        if (!isValidTime) {
            // Not on time, streak doesn't increase, but DOES IT RESET?
            // Strict mode: Yes. Friendly mode: Maybe not?
            // Let's implement strict for now but maybe just don't increment if missed?
            // User request: "Sleep Streaks... 3, 7, 30 days in a row" implies strict "in a row".
            // So if they miss a night, streak broken.
            
            // Logic: If last sleep date was YESTERDAY, and today is invalid -> Streak Broken (Reset to 0 or 1?)
            // If they sleep "late", it resets.
            
            // Wait, if they just open the app at 2 PM, we shouldn't reset. We only reset on "Sleep".
            // Actually, we should check if they missed yesterday.
            return StreakResult(settings, emptyList())
        }

        // 2. Logic for Streak Increment
        val lastDateStr = settings.lastSleepDate
        val lastDate = if (lastDateStr != null) LocalDate.parse(lastDateStr, dateFormatter) else null
        
        var newStreak = settings.sleepStreakCount
        
        if (lastDate == yesterday) {
            // Consecutive day!
            newStreak += 1
        } else if (lastDate == today) {
            // Already slept today (maybe nap?), don't increment, keep same
        } else {
            // Missed a day or more, or first time
            newStreak = 1
        }

        // 3. Badges
        val newBadges = settings.unlockedBadges.toMutableSet()
        val earnedNow = mutableListOf<String>()

        BadgeRepository.badges.forEach { badge ->
            if (newStreak >= badge.requiredStreak && !newBadges.contains(badge.id)) {
                newBadges.add(badge.id)
                earnedNow.add(badge.id)
            }
        }

        return StreakResult(
            settings = settings.copy(
                sleepStreakCount = newStreak,
                lastSleepDate = today.format(dateFormatter),
                unlockedBadges = newBadges
            ),
            newBadgesEarned = earnedNow
        )
    }

    private fun isWithinConsistencyWindow(now: LocalDateTime, targetHour: Int, targetMinute: Int): Boolean {
        // Construct target for TODAY
        val todayTarget = LocalDateTime.of(now.toLocalDate(), LocalTime.of(targetHour, targetMinute))
        
        // Construct target for YESTERDAY (e.g. if now is 12:15 AM and target is 11:45 PM)
        val yesterdayTarget = todayTarget.minusDays(1)
        
        // Construct target for TOMORROW (e.g. if now is 11:45 PM and target is 12:15 AM next day?) 
        // Usually bedtime is PM.
        val tomorrowTarget = todayTarget.plusDays(1)

        val windowMinutes = 45L // 45 mins grace

        return (abs(ChronoUnit.MINUTES.between(now, todayTarget)) <= windowMinutes) ||
               (abs(ChronoUnit.MINUTES.between(now, yesterdayTarget)) <= windowMinutes) ||
               (abs(ChronoUnit.MINUTES.between(now, tomorrowTarget)) <= windowMinutes)
    }
}
