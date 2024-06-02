package db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import hockey.data.ChatEntity
import hockey.data.ChatMessageEntity
import hockey.data.ListingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import me.tatarka.inject.annotations.Inject
import personallm.db.Database

@Inject
class AppDatabase(
    private val db: Database,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun listingItems(): Flow<List<ListingItem>> {
        val queries = db.listingItemQueries.selectAll()
        return queries.asFlow().mapToList(scope.coroutineContext)
    }

    fun chatMessages(chatId: String): Flow<List<ChatMessageEntity>> {
        return db.chatMessageEntityQueries.selectMessagesForChat(chatId).asFlow()
            .mapToList(scope.coroutineContext)
    }

    fun chats(): Flow<List<ChatEntity>> {
        return db.chatEntityQueries.selectAllChats().asFlow().mapToList(scope.coroutineContext)
    }

    suspend fun insertChat(entity: ChatEntity) {
        db.chatEntityQueries.insertOrReplaceChat(
            id = entity.id,
            version = entity.version,
            creationTimestamp = entity.creationTimestamp,
            lastMessageTimestamp = entity.lastMessageTimestamp,
            summary = entity.summary
        )
    }

    suspend fun findChatById(id: String): ChatEntity? {
        return db.chatEntityQueries.selectChatById(id).asFlow()
            .mapToOneOrNull(scope.coroutineContext).firstOrNull()
    }

    suspend fun insertChatMessage(entity: ChatMessageEntity) {
        db.chatMessageEntityQueries.inserFullMessage(entity)
    }

    suspend fun insertListingItem(itemText: String) {
        db.listingItemQueries.insert(text = itemText)
    }
}