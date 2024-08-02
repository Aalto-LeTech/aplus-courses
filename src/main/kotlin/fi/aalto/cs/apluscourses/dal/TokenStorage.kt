package fi.aalto.cs.apluscourses.dal

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.OneTimeString
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object TokenStorage {
    private const val A_COURSES_PLUGIN: String = "A+ Courses Plugin"
    private const val KEY: String = "auth_token"

    val credentialAttributes: CredentialAttributes = CredentialAttributes(
        generateServiceName(A_COURSES_PLUGIN, KEY)
    )

    fun store(password: OneTimeString?): Boolean {
        val credentials = if (password == null) null else Credentials(credentialAttributes.userName, password)
        PasswordSafe.instance.set(credentialAttributes, credentials)
        return !PasswordSafe.instance.isPasswordStoredOnlyInMemory(credentialAttributes, credentials!!)
    }

    fun remove() {
        PasswordSafe.instance.set(credentialAttributes, null)
    }

    fun getToken(): OneTimeString? =
        PasswordSafe.instance.getPassword(credentialAttributes)?.let { OneTimeString(it) }

    fun isTokenSet(): Boolean = PasswordSafe.instance.getPassword(credentialAttributes) != null
}
