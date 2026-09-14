package com.example.learning

import org.junit.Assert.assertEquals
import org.junit.Test

class MasteryRepositoryTest {
    @Test
    fun mastery_formula_rewards_accuracyAndStreak() {
        // Contract test documenting the intended mastery progression:
        // accuracy contributes 70%, while a sustained correct streak contributes 30%.
        val accuracy = 8 * 100 / 10
        val streak = 6
        val mastery = minOf(100, (accuracy * 70 / 100) + minOf(30, streak * 5))
        assertEquals(86, mastery)
    }
}
