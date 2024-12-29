package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noisevisionsoftware.szytadieta.domain.model.Weight
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val weightsCollection = "weights"

    suspend fun addWeight(weight: Weight): Result<Unit> = try {
        firestore.collection(weightsCollection)
            .document(weight.id)
            .set(weight)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserWeights(userId: String): Result<List<Weight>> = try {
        val snapshot = firestore.collection(weightsCollection)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()

        Result.success(snapshot.documents.mapNotNull { it.toObject(Weight::class.java) })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteWeight(weightId: String): Result<Unit> = try {
        firestore.collection(weightsCollection)
            .document(weightId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}