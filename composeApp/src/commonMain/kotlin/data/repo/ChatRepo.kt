package data.repo

import data.ChatCompletionsRequestBody
import data.ChatCompletionsRequestBody.MessageItem.Companion.ROLE_ASSISTANT
import data.ChatCompletionsRequestBody.MessageItem.Companion.ROLE_SYSTEM
import data.ChatCompletionsRequestBody.MessageItem.Companion.ROLE_USER
import data.OpenAIAPIWrapper
import db.AppDatabase
import di.Singleton
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

@Singleton
@Inject
class ChatRepo(
    private val api: OpenAIAPIWrapper,
    private val db: AppDatabase,
    private val signaling: InAppSignaling,
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    suspend fun submitNew(orgChatId: String?, prompt: String, model: OpenAIAPIWrapper.Model, template: Template?): String {
        val current = orgChatId?.let { id ->
            val messages = db.chatMessages(id).firstOrNull().orEmpty()
            val chat = db.findChatById(id)?.toChat()!!
            chat.copy(prevMessages = messages.map { it.toDomain() })
        } ?: Chat(
            id = randomUUID(),
            prevMessages = emptyList(),
            lastMessage = null,
            createdAt = Clock.System.now(),
            lastMessageAt = Clock.System.now(),
            summary = "",
        ).also { curr ->
            scope.launch {
                db.insertChat(curr.toEntity())
            }
        }

        val userMessage = ChatMessage(
            id = randomUUID(),
            content = prompt,
            fromUser = true,
            timestamp = Clock.System.now(),
            finished = true
        )

        scope.launch {
            db.insertChatMessage(userMessage.toEntity(current.id))
        }

        scope.launch {
            val systemMsg = template?.let {
                listOf(ChatCompletionsRequestBody.Message(
                    role = ROLE_SYSTEM,
                    content = listOf(ChatCompletionsRequestBody.MessageItem(text = it.prompt))
                ))
            } ?: emptyList()
            val prevMsgs = current.prevMessages.map {
                ChatCompletionsRequestBody.Message(
                    role = if (it.fromUser) ROLE_USER else ROLE_ASSISTANT,
                    content = listOf(ChatCompletionsRequestBody.MessageItem(text = it.content))
                )
            }
            api.getChatCompletions(prompt = prompt, prevMessages = systemMsg + prevMsgs, model = model).collect { resp ->
                resp.doOnError {
                    signaling.handleGenericError(it)
                }.doOnSuccessSusp {
                    val msg = ChatMessage(
                        id = it.response.id,
                        content = it.message,
                        fromUser = false,
                        timestamp = Clock.System.now(),
                        finished = it.response.done(),
                    )
                    db.insertChatMessage(msg.toEntity(current.id))
                }
            }
            updateSummary(chatId = current.id, prompt = prompt, prevMessages = prevMsgs)
        }
        return current.id
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

    fun chatsFlow(): Flow<List<Chat>> {
        return db.chats().map { entities ->
            entities.map {
                it.toChat()
            }
        }
    }

    private fun updateSummary(chatId: String, prompt: String, prevMessages: List<ChatCompletionsRequestBody.Message>) {
        // TODO: logic to update it more often?
        if (prevMessages.isNotEmpty()) return
        val msgs = listOf(
            ChatCompletionsRequestBody.Message(
                role = ROLE_USER,
                content = listOf(ChatCompletionsRequestBody.MessageItem(text = prompt))
            )
        )
        scope.launch {
            api.getChatSummary(msgs).doOnError {
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
    )

    private fun ChatMessage.toEntity(chatId: String) = ChatMessageEntity(
        id = id,
        content = content,
        timestamp = timestamp.toEpochMilliseconds(),
        fromUser = if (fromUser) 1L else 0L,
        finished = if (finished) 1L else 0L,
        chatId = chatId,
    )

    private fun Chat.toEntity() = ChatEntity(
        id = id,
        version = 1,
        creationTimestamp = createdAt.toEpochMilliseconds(),
        lastMessageTimestamp = lastMessageAt.toEpochMilliseconds(),
        summary = summary,
    )

    private fun ChatEntity.toChat() = Chat(
        id = id,
        createdAt = Instant.fromEpochMilliseconds(creationTimestamp),
        lastMessageAt = Instant.fromEpochMilliseconds(lastMessageTimestamp),
        summary = summary,
        prevMessages = emptyList(),
        lastMessage = null,
    )
}

fun <K, V> Map<K, V>.withUpdated(key: K, value: (V?) -> V?): Map<K, V> {
    return toMutableMap().apply {
        val current = this[key]
        val new = value(current)
        if (new != null) {
            this[key] = new
        }
    }.toMap()
}

data class Chat(
    val id: String,
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
)
