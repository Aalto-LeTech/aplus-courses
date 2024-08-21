package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.XMap
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64
import kotlin.io.path.readBytes

@Service(Service.Level.PROJECT)
@State(name = "Duplicate Submission Checker", storages = [Storage("aplus_project.xml")])
class DuplicateSubmissionChecker(val project: Project) :
    SimplePersistentStateComponent<DuplicateSubmissionChecker.State>(State()) {

    class State : BaseState() {
        @get:XMap(
            propertyElementName = "submissionHashes",
            entryTagName = "exercise",
            keyAttributeName = "id",
            valueAttributeName = "hashes",
        )
        var submissionHashes: MutableMap<Long, String> = mutableMapOf()
        fun increment() = incrementModificationCount()
    }

    private fun hashFileToString(digest: MessageDigest, path: Path): String =
        Base64.getEncoder().encodeToString(digest.digest(path.readBytes()))

    private fun hashAllFiles(files: Map<String, Path>): String {
        val shaDigest = MessageDigest.getInstance("SHA-256")
        val submissionString = files.entries
            .sortedBy { it.key }
            .joinToString(",") { "${it.key}|${hashFileToString(shaDigest, it.value)}" }

        return Base64.getEncoder().encodeToString(shaDigest.digest(submissionString.toByteArray()))
    }

    fun isDuplicateSubmission(exerciseId: Long, files: Map<String, Path>): Boolean {
        println("Checking duplicate submission $exerciseId $files ${state.submissionHashes}")
        val currentHash = hashAllFiles(files)
        val existingHashes = state.submissionHashes[exerciseId]?.split(";") ?: listOf()
        return existingHashes.contains(currentHash)
    }

    fun onAssignmentSubmitted(exerciseId: Long, files: Map<String, Path>) {
        println("Assignment submitted $exerciseId $files")
        val currentHash = hashAllFiles(files)
        val updatedHashes = (state.submissionHashes[exerciseId]?.split(";") ?: listOf()) + currentHash
        state.submissionHashes[exerciseId] = updatedHashes.joinToString(";")
        state.increment()
    }

    companion object {
        fun getInstance(project: Project): DuplicateSubmissionChecker {
            return project.service<DuplicateSubmissionChecker>()
        }
    }
}