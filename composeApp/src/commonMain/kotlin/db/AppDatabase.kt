package db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import di.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import personallm.data.ChatEntity
import personallm.data.ChatMessageEntity
import personallm.data.ListingItem
import personallm.data.TemplateEntity
import personallm.db.Database

@Singleton
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

    fun chats(sortAsc: Boolean): Flow<List<ChatEntity>> {
        return (if (sortAsc) db.chatEntityQueries.selectAllChatsAsc() else db.chatEntityQueries.selectAllChatsDesc()).asFlow().mapToList(scope.coroutineContext)
    }

    fun templates(): Flow<List<TemplateEntity>> {
        return db.templateEntityQueries.selectAllTemplates().asFlow()
            .mapToList(scope.coroutineContext)
    }

    suspend fun insertOrUpdateTemplate(template: TemplateEntity) {
        db.templateEntityQueries.inserFullTemplate(template)
    }

    fun templateById(id: String): Flow<TemplateEntity> {
        return db.templateEntityQueries.selectTemplateById(id).asFlow()
            .mapToOneOrNull(scope.coroutineContext).filterNotNull()
    }

    suspend fun deleteTemplatesByIds(ids: Set<String>) {
        db.templateEntityQueries.deleteTemplatesByIDs(ids)
    }

    suspend fun insertOrUpdateChat(entity: ChatEntity) {
        db.chatEntityQueries.insertOrReplaceChat(
            id = entity.id,
            templateId = entity.templateId,
            version = entity.version,
            creationTimestamp = entity.creationTimestamp,
            lastMessageTimestamp = entity.lastMessageTimestamp,
            summary = entity.summary
        )
    }

    suspend fun updateChatSummary(id: String, summary: String) {
        db.chatEntityQueries.updateChatSummaryById(
            summary = summary,
            id = id,
        )
    }

    fun chatById(id: String): Flow<ChatEntity> {
        return db.chatEntityQueries.selectChatById(id).asFlow()
            .mapToOneOrNull(scope.coroutineContext).filterNotNull()
    }

    suspend fun findChatById(id: String): ChatEntity? {
        return db.chatEntityQueries.selectChatById(id).asFlow()
            .mapToOneOrNull(scope.coroutineContext).firstOrNull()
    }

    suspend fun insertChatMessage(entity: ChatMessageEntity) {
        db.chatMessageEntityQueries.inserFullMessage(entity)
    }

    suspend fun deleteChatMessage(id: String) {
        db.chatMessageEntityQueries.deleteById(id)
    }

    suspend fun deleteChats(ids: Set<String>) {
        db.chatEntityQueries.deleteChatsByIds(ids)
    }

    suspend fun deleteChatMessagesAfter(timestamp: Instant) {
        db.chatMessageEntityQueries.deleteMessagesAboveTimestamp(timestamp = timestamp.toEpochMilliseconds())
    }

    suspend fun insertListingItem(itemText: String) {
        db.listingItemQueries.insert(text = itemText)
    }

    suspend fun insertTemplate(entity: TemplateEntity) {
        db.templateEntityQueries.inserFullTemplate(entity)
    }
}
