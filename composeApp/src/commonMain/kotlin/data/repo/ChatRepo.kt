package data.repo

import data.CompletionsApi
import data.Message
import data.Model
import data.combinedMessage
import db.AppDatabase
import di.Singleton
import feature.sharedimage.ImageLocation
import feature.sharedimage.ImageResolver
import feature.sharedimage.SharedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import personallm.data.ChatEntity
import personallm.data.ChatMessageEntity
import util.randomUUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Singleton
@Inject
class ChatRepo(
    private val api: CompletionsApi,
    private val db: AppDatabase,
    private val signaling: InAppSignaling,
    private val imageResolver: ImageResolver,
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    suspend fun submitNew(
        orgChatId: String?,
        prompt: String,
        image: SharedImage?,
        model: Model,
        template: Template?
    ): String {
        val current = orgChatId?.let { id ->
            val messages = db.chatMessages(id).firstOrNull().orEmpty()
            val chat = db.findChatById(id)?.toChat()!!
            chat.copy(prevMessages = messages.map { it.toDomain() }, templateId = template?.id)
        } ?: Chat(
            id = randomUUID(),
            templateId = template?.id,
            prevMessages = emptyList(),
            lastMessage = null,
            createdAt = Clock.System.now(),
            lastMessageAt = Clock.System.now(),
            summary = "",
        )

        scope.launch {
            db.insertOrUpdateChat(current.toEntity())
        }

        submitMessage(
            chatId = current.id,
            model = model,
            orgMessage = null,
            prompt = prompt,
            image = image,
            prevMessages = current.prevMessages,
            template = template,
        )
        return current.id
    }

    suspend fun delete(chatId: String, messageId: String) {
        scope.launch {
            val messages = db.chatMessages(chatId).firstOrNull().orEmpty()
            val orgMessage = messages.first { it.id == messageId }.toDomain()
            db.deleteChatMessage(orgMessage.id)
            db.deleteChatMessagesAfter(timestamp = orgMessage.timestamp)
        }
    }

    // TODO: From user
    suspend fun edit(chatId: String, messageId: String, newPrompt: String, image: SharedImage?, isFromUser: Boolean, model: Model, template: Template?) {
        val messages = db.chatMessages(chatId).firstOrNull().orEmpty()
        val orgMessage = messages.first { it.id == messageId }
        val prevMessages = messages.filter { it.timestamp < orgMessage.timestamp }.map { it.toDomain() }

        submitMessage(
            chatId = chatId,
            model = model,
            orgMessage = orgMessage.toDomain(),
            prompt = newPrompt,
            image = image,
            prevMessages = prevMessages,
            template = template,
        )
    }

    fun flow(chatId: String): Flow<List<ChatMessage>> {
        return db.chatMessages(chatId).map { dbChats ->
            dbChats.map { it.toDomain() }
        }
    }

    fun chatById(chatId: String): Flow<Chat> {
        return db.chatById(chatId).map {
            it.toChat()
        }
    }

    fun chatsFlow(sortAsc: Boolean): Flow<List<Chat>> {
        return db.chats(sortAsc = sortAsc).map { entities ->
            entities.map {
                it.toChat()
            }
        }
    }

    private suspend fun submitMessage(
        chatId: String,
        orgMessage: ChatMessage?,
        prompt: String,
        image: SharedImage?,
        prevMessages: List<ChatMessage>,
        model: Model,
        template: Template?
    ) {
        val storedImageLocation = image?.storeLocally(imageResolver)

        val userMessage = orgMessage?.copy(
            content = prompt,
            imageLocation = storedImageLocation,
        ) ?: ChatMessage(
            id = randomUUID(),
            content = prompt,
            fromUser = true,
            timestamp = Clock.System.now(),
            finished = true,
            imageLocation = storedImageLocation,
            error = false,
        )

        scope.launch {
            if (orgMessage != null) {
                db.deleteChatMessagesAfter(timestamp = orgMessage.timestamp)
            }
            db.insertChatMessage(userMessage.toEntity(chatId))
        }

        scope.launch {
            val systemMsg = template?.let {
                listOf(
                    Message(
                        role = Message.Role.SYSTEM,
                        content = listOf(Message.MessageItem(text = it.prompt))
                    )
                )
            } ?: emptyList()
            val prevMsgs = prevMessages.filterNot { it.error }.map {
                Message(
                    role = if (it.fromUser) Message.Role.USER else Message.Role.ASSISTANT,
                    content = listOf(Message.MessageItem(text = it.content))
                )
            }

            val imageEncoded = image?.toByteArray()?.let { byteArray ->
                Base64.encode(byteArray)
            }
            var anyError = false
            api.getChatCompletions(
                prompt = prompt,
                prevMessages = systemMsg + prevMsgs,
                model = model,
                imageEncoded = imageEncoded,
            ).collect { resp ->
                resp.doOnErrorSusp {
                    anyError = true
                    signaling.handleGenericError(it)
                    val errorMsg = it.error.combinedMessage()

                    val msg = ChatMessage(
                        id = randomUUID(),
                        content = "Error:\n$errorMsg",
                        fromUser = false,
                        timestamp = Clock.System.now(),
                        finished = true,
                        imageLocation = null,
                        error = true,
                    )
                    db.insertChatMessage(msg.toEntity(chatId))

                }.doOnSuccessSusp {
                    val msg = ChatMessage(
                        id = it.id,
                        content = it.message,
                        fromUser = false,
                        timestamp = Clock.System.now(),
                        finished = it.done,
                        imageLocation = null,
                        error = false,
                    )
                    db.insertChatMessage(msg.toEntity(chatId))
                }
            }
            if (!anyError) {
                updateSummary(chatId = chatId, prompt = prompt, prevMessages = prevMsgs, model = model)
            }

        }
        // TODO: Update chat (timestamp, template, etc.)
    }

    private fun updateSummary(
        chatId: String,
        prompt: String,
        prevMessages: List<Message>,
        model: Model,
    ) {
        // TODO: logic to update it more often?
        if (prevMessages.isNotEmpty()) return
        val msgs = listOf(
            Message(
                role = Message.Role.USER,
                content = listOf(Message.MessageItem(text = prompt))
            )
        )
        scope.launch {
            api.getChatSummary(msgs, model).doOnError {
                signaling.handleGenericError(it)
            }.doOnSuccessSusp { summary ->
                db.updateChatSummary(id = chatId, summary = summary)
            }
        }
    }

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        content = content,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        fromUser = fromUser == 1L,
        finished = finished == 1L,
        imageLocation = localImageUri?.let { ImageLocation.StoredUri(it) },
        error = error == 1L,
    )

    private fun ChatMessage.toEntity(chatId: String) = ChatMessageEntity(
        id = id,
        content = content,
        timestamp = timestamp.toEpochMilliseconds(),
        fromUser = if (fromUser) 1L else 0L,
        finished = if (finished) 1L else 0L,
        chatId = chatId,
        localImageUri = imageLocation?.uri,
        error = if (error) 1L else 0L,
    )

    private fun Chat.toEntity() = ChatEntity(
        id = id,
        templateId = templateId,
        version = 1,
        creationTimestamp = createdAt.toEpochMilliseconds(),
        lastMessageTimestamp = lastMessageAt.toEpochMilliseconds(),
        summary = summary,
    )

    private fun ChatEntity.toChat() = Chat(
        id = id,
        templateId = templateId,
        createdAt = Instant.fromEpochMilliseconds(creationTimestamp),
        lastMessageAt = Instant.fromEpochMilliseconds(lastMessageTimestamp),
        summary = summary,
        prevMessages = emptyList(),
        lastMessage = null,
    )
}

data class Chat(
    val id: String,
    val templateId: String?,
    val summary: String,
    val createdAt: Instant,
    val lastMessageAt: Instant,
    val prevMessages: List<ChatMessage>,
    val lastMessage: ChatMessage?,
)

data class ChatMessage(
    val id: String,
    val content: String,
    val timestamp: Instant,
    val fromUser: Boolean,
    val finished: Boolean,
    val imageLocation: ImageLocation.StoredUri?,
    val error: Boolean,
)
