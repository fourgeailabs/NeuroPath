package com.fourgeailabs.neuropath.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Idempotency record for every stars/gems grant.
 *
 * A grant is applied only when its [grantKey] has never been recorded: the
 * insert-or-ignore plus the balance increment run in a single Room transaction
 * ([com.fourgeailabs.neuropath.data.local.RewardGrantDao.grantOnce]), so a
 * retried or concurrently duplicated grant can never double-award. The key
 * identifies the logical reward event — e.g.
 * "lesson_completion:<lessonId>:attempt:<n>" — and is chosen by the caller.
 * Deleting the profile cascades to its grant records — no orphaned rows.
 */
@Entity(
    tableName = "reward_grants",
    foreignKeys = [
        ForeignKey(
            entity = ChildProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class RewardGrantEntity(
    /** Idempotency key for the reward event; duplicates are ignored, never re-applied. */
    @PrimaryKey
    val grantKey: String,
    val profileId: Long,
    val stars: Int,
    val gems: Int,
    /** Machine-readable reason, e.g. "lesson_completion" or "manual_award". Never child PII. */
    val reason: String,
    val grantedAt: Long = System.currentTimeMillis()
)
