package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.UserEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Data model for Cloud Firestore user profile
 */
data class FirestoreUserProfile(
    val userId: Long = 0L,
    val displayName: String = "",
    val profilePicture: String = "avatar_user",
    val bio: String = "",
    val level: Int = 1,
    val exp: Int = 0,
    val vipTier: Int = 0,
    val isOfficialVerified: Boolean = false,
    val equippedMedal: String = "",
    val accumulatedMedals: List<String> = emptyList(),
    val medalsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val giftsReceivedCount: Int = 0,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

/**
 * Service managing profile fetching, live observing, and syncing with Google Cloud Firestore
 */
class FirestoreProfileService(private val context: Context) {

    private val tag = "FirestoreProfile"

    private val firestore: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(tag, "Firestore not initialized: ${e.message}")
            null
        }

    /**
     * Check if Firestore service is available
     */
    fun isAvailable(): Boolean = firestore != null

    /**
     * Fetch user profile from Firestore once
     */
    suspend fun fetchProfile(userId: Long): FirestoreUserProfile? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val doc = db.collection("users").document(userId.toString()).get().await()
            if (doc.exists()) {
                mapDocumentToProfile(doc.data ?: emptyMap(), userId)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching profile for $userId: ${e.message}")
            null
        }
    }

    /**
     * Real-time flow observing Firestore user document
     */
    fun observeProfile(userId: Long): Flow<FirestoreUserProfile?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection("users").document(userId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed for $userId: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = mapDocumentToProfile(snapshot.data ?: emptyMap(), userId)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    /**
     * Sync local user and accumulated medals to Firestore
     */
    suspend fun syncProfileToFirestore(
        user: UserEntity,
        accumulatedMedals: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        try {
            val data = hashMapOf(
                "userId" to user.userId,
                "displayName" to user.nickname,
                "profilePicture" to user.avatarUrl,
                "bio" to user.bio,
                "level" to user.level,
                "exp" to user.exp,
                "vipTier" to user.vipTier,
                "isOfficialVerified" to user.isOfficialVerified,
                "equippedMedal" to user.equippedBadge,
                "accumulatedMedals" to accumulatedMedals,
                "medalsCount" to accumulatedMedals.size,
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "giftsReceivedCount" to user.giftsReceivedCount,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(user.userId.toString())
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync profile to Firestore: ${e.message}")
            false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDocumentToProfile(data: Map<String, Any>, defaultUserId: Long): FirestoreUserProfile {
        val medalsList = (data["accumulatedMedals"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        return FirestoreUserProfile(
            userId = (data["userId"] as? Number)?.toLong() ?: defaultUserId,
            displayName = data["displayName"]?.toString() ?: data["nickname"]?.toString() ?: "",
            profilePicture = data["profilePicture"]?.toString() ?: data["avatarUrl"]?.toString() ?: "avatar_user",
            bio = data["bio"]?.toString() ?: "",
            level = (data["level"] as? Number)?.toInt() ?: 1,
            exp = (data["exp"] as? Number)?.toInt() ?: 0,
            vipTier = (data["vipTier"] as? Number)?.toInt() ?: 0,
            isOfficialVerified = data["isOfficialVerified"] as? Boolean ?: false,
            equippedMedal = data["equippedMedal"]?.toString() ?: data["equippedBadge"]?.toString() ?: "",
            accumulatedMedals = medalsList,
            medalsCount = (data["medalsCount"] as? Number)?.toInt() ?: medalsList.size,
            followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
            followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
            giftsReceivedCount = (data["giftsReceivedCount"] as? Number)?.toInt() ?: 0,
            lastSyncedAt = (data["lastSyncedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
