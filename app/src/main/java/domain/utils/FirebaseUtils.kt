package com.example.project_helper.utils

import com.google.firebase.firestore.FieldValue

object FirebaseUtils {
    fun arrayUnion(vararg elements: Any): FieldValue {
        return FieldValue.arrayUnion(*elements)
    }
}