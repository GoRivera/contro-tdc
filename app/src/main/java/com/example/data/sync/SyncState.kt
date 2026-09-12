package com.example.data.sync

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String, val timestampMillis: Long = System.currentTimeMillis()) : SyncState()
    data class Error(val errorMessage: String) : SyncState()
    object FirebaseNotConfigured : SyncState()
}

data class FirebaseAccountInfo(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)
