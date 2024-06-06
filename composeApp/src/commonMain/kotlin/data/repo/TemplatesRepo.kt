package data.repo

import data.OpenAIAPIWrapper
import db.AppDatabase
import di.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import personallm.data.TemplateEntity

@Singleton
@Inject
class TemplatesRepo(
    private val api: OpenAIAPIWrapper,
    private val db: AppDatabase,
    private val signaling: InAppSignaling,
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun templatesFlow(): Flow<List<Template>> {
        return db.templates().map { entities ->
            entities.map {
                it.toTemplate()
            }
        }
    }

    private fun Template.toEntity() = TemplateEntity(
        id = id,
        version = 1,
        creationTimestamp = createdAt.toEpochMilliseconds(),
        updateTimestamp = updatedAt?.toEpochMilliseconds(),
        prompt = prompt,
        title = title,
    )

    private fun TemplateEntity.toTemplate() = Template(
        id = id,
        createdAt = Instant.fromEpochMilliseconds(creationTimestamp),
        updatedAt = updateTimestamp?.let { Instant.fromEpochMilliseconds(it) },
        title = title,
        prompt = prompt,
    )
}

data class Template(
    val id: String,
    val title: String,
    val prompt: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
)
