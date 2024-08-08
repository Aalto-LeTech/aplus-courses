package fi.aalto.cs.apluscourses.services

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.OneTimeString
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.application
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.people.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls

@Service(Service.Level.APP)
class TokenStorage(private val cs: CoroutineScope) {
    @NonNls
    private val serviceName: String = "A+ Courses Plugin"

    @NonNls
    private val serviceKey: String = "auth_token"

    private val credentialAttributes: CredentialAttributes = CredentialAttributes(
        generateServiceName(serviceName, serviceKey)
    )

    fun storeAndCheck(password: OneTimeString?, project: Project, callback: (User?) -> Unit) {
        cs.launch {
            store(password)
            try {
                val user = APlusApi.me().get(project)
                callback(user)
            } catch (_: Exception) {
                remove()
                callback(null)
                return@launch
            }
        }
    }

    suspend fun store(password: OneTimeString?) {
        val credentials = if (password == null) null else Credentials(credentialAttributes.userName, password)
        withContext(Dispatchers.IO) {
            PasswordSafe.instance.set(credentialAttributes, credentials)
        }
    }

    fun remove() {
        cs.launch {
            withContext(Dispatchers.IO) {
                PasswordSafe.instance.set(credentialAttributes, null)
            }
        }
    }

    suspend fun getToken(): OneTimeString? =
        withContext(Dispatchers.IO) {
            PasswordSafe.instance.getPassword(credentialAttributes)?.let { OneTimeString(it) }
        }


    suspend fun isTokenSet(): Boolean =
        withContext(Dispatchers.IO) {
            PasswordSafe.instance.getPassword(credentialAttributes) != null
        }

    companion object {
        fun getInstance(): TokenStorage = application.service<TokenStorage>()
    }
}
