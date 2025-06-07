package com.example.project_helper.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreApiKeyFetcher(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun fetchDeepSeekApiKey(): String? {
        return try {
            val documentSnapshot = db.collection("Deepsekk_api").document("Toy4u0MjoDTrUwynMD8y").get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.getString("api")
            } else {
                null
            }
        } catch (e: Exception) {
            println("Ошибка при чтении API ключа из Firestore: ${e.message}")
            null
        }
    }
}
