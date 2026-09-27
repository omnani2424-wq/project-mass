package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String? = null
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user" or "model"
    val content: String,
    val imageUri: String? = null,
    val sourcesJson: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val mediaType: String? = null, // "image", "music", "video"
    val mediaUri: String? = null
)

data class GroundingSource(
    val title: String,
    val uri: String
)
