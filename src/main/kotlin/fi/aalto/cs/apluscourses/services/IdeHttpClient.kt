package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/** Shares a redirect-capable, unauthenticated transport for course configs and public downloads. */
@Service(Service.Level.APP)
class IdeHttpClient : Disposable {
    val client: HttpClient = HttpClient(OkHttp) {
        followRedirects = true
        engine { config { trustIdeCertificates().allowConcurrentRequests() } }
    }

    override fun dispose() {
        client.close()
    }

    companion object {
        fun getInstance(): IdeHttpClient = service()
    }
}
