package fi.aalto.cs.apluscourses.services

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.OneTimeString
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.application
import fi.aalto.cs.apluscourses.model.mapping.toUser
import fi.aalto.cs.apluscourses.model.people.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import fi.aalto.cs.aplus.client.AplusClient
import io.ktor.http.Url

@Service(Service.Level.APP)
class TokenStorage(private val cs: CoroutineScope) {
    @NonNls
    private val serviceName: String = "A+ Courses Plugin Token"

    private fun credentialAttributes(baseUrl: String): CredentialAttributes = CredentialAttributes(
        generateServiceName(serviceName, tokenServiceKey(baseUrl))
    )

    fun storeAndCheck(password: OneTimeString?, project: Project, callback: (User?) -> Unit) {
        val aplus = CoursesClient.getInstance(project).aplus
        val attributes = credentialAttributes(aplus.baseUrl)
        cs.launch {
            store(password, attributes)
            try {
                val user = toUser(aplus.me())
                callback(user)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                store(null, attributes)
                callback(null)
                return@launch
            }
        }
    }

    private suspend fun store(password: OneTimeString?, attributes: CredentialAttributes) {
        val credentials = if (password == null) null else Credentials(attributes.userName, password)
        withContext(Dispatchers.IO) { PasswordSafe.instance[attributes] = credentials }
    }

    fun remove(project: Project) {
        val attributes = credentialAttributes(CoursesClient.getInstance(project).aplus.baseUrl)
        cs.launch { store(null, attributes) }
    }

    suspend fun getToken(baseUrl: String): OneTimeString? =
        withContext(Dispatchers.IO) {
            PasswordSafe.instance.getPassword(credentialAttributes(baseUrl))?.let { OneTimeString(it) }
        }

    suspend fun isTokenSet(project: Project): Boolean =
        getToken(CoursesClient.getInstance(project).aplus.baseUrl)?.isNotBlank() == true

    companion object {
        fun getInstance(): TokenStorage = application.service<TokenStorage>()
    }
}

/** Credentials are shared by URLs with the same scheme, host and port. */
internal fun tokenServiceKey(baseUrl: String): String {
    val url = Url(AplusClient.normalizeBaseUrl(baseUrl))
    val host = url.host.lowercase()
    return "${url.protocol.name}://$host:${url.port}"
}
