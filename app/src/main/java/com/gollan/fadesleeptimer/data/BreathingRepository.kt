package com.gollan.fadesleeptimer.data

data class BreathingPattern(
    val id: String,
    val name: String,
    val textPattern: String, // e.g., "4-7-8"
    val inhaleMs: Long,      // Duration in millis
    val holdMs: Long,
    val exhaleMs: Long,
    val postHoldMs: Long,    // For "Box Breathing" (Hold after exhale)
    val description: String
)

object BreathingRepository {
    val patterns = listOf(
        BreathingPattern(
            id = "deep", 
            name = "Deep Sleep", 
            textPattern = "4-7-8", 
            inhaleMs = 4000, holdMs = 7000, exhaleMs = 8000, postHoldMs = 0, 
            description = "Dr. Weil's technique to tranquilize the nervous system."
        ),
        BreathingPattern(
            id = "box", 
            name = "Tactical Calm", 
            textPattern = "4-4-4-4", 
            inhaleMs = 4000, holdMs = 4000, exhaleMs = 4000, postHoldMs = 4000, 
            description = "Navy SEAL technique for focus and stress control."
        ),
        BreathingPattern(
            id = "reset", 
            name = "Instant Reset", 
            textPattern = "4-6", 
            inhaleMs = 4000, holdMs = 0, exhaleMs = 6000, postHoldMs = 0, 
            description = "Extended exhale to trigger relaxation response."
        )
    )

    fun getPattern(id: String): BreathingPattern {
        return patterns.find { it.id == id } ?: patterns.first()
    }
}
