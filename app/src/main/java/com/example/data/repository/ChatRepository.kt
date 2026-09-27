package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val chatDao: ChatDao
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun getSessionById(sessionId: String): ChatSessionEntity? = withContext(Dispatchers.IO) {
        chatDao.getSessionById(sessionId)
    }

    suspend fun createNewSession(title: String = "New Session", category: String? = null): ChatSessionEntity = withContext(Dispatchers.IO) {
        val newSession = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            category = category
        )
        chatDao.insertSession(newSession)
        newSession
    }

    suspend fun saveUserMessage(
        sessionId: String,
        content: String,
        imageUri: String? = null
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = "user",
            content = content,
            imageUri = imageUri,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)

        // Check if session is using default title, rename to first user prompt
        val session = chatDao.getSessionById(sessionId)
        if (session != null && (session.title == "New Session" || session.title.startsWith("New Laboratory"))) {
            val autoTitle = if (content.length > 36) {
                content.take(36).trim() + "..."
            } else {
                content.trim()
            }
            if (autoTitle.isNotBlank()) {
                chatDao.renameSession(sessionId, autoTitle)
            }
        } else {
            // Update session timestamp
            if (session != null) {
                chatDao.renameSession(sessionId, session.title, System.currentTimeMillis())
            }
        }
        message
    }

    suspend fun saveAssistantMessage(
        sessionId: String,
        content: String,
        sources: List<GroundingSource> = emptyList(),
        isError: Boolean = false,
        mediaType: String? = null,
        mediaUri: String? = null
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
        val sourcesJson = if (sources.isNotEmpty()) {
            val jsonArray = JSONArray()
            for (s in sources) {
                val obj = JSONObject()
                obj.put("title", s.title)
                obj.put("uri", s.uri)
                jsonArray.put(obj)
            }
            jsonArray.toString()
        } else null

        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = "model",
            content = content,
            sourcesJson = sourcesJson,
            timestamp = System.currentTimeMillis(),
            isError = isError,
            mediaType = mediaType,
            mediaUri = mediaUri
        )
        chatDao.insertMessage(message)

        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            chatDao.renameSession(sessionId, session.title, System.currentTimeMillis())
        }
        message
    }

    suspend fun renameSession(sessionId: String, newTitle: String) = withContext(Dispatchers.IO) {
        chatDao.renameSession(sessionId, newTitle)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun clearAllSessions() = withContext(Dispatchers.IO) {
        chatDao.deleteAllSessions()
    }

    suspend fun saveBitmapLocally(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "mass_img_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file).toString()
    }

    suspend fun saveBytesLocally(bytes: ByteArray, extension: String, prefix: String = "mass_media"): String = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.$extension")
        FileOutputStream(file).use { out ->
            out.write(bytes)
        }
        Uri.fromFile(file).toString()
    }
}
