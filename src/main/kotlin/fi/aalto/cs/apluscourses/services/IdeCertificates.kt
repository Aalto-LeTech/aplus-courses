package fi.aalto.cs.apluscourses.services

import com.intellij.util.net.ssl.CertificateManager
import okhttp3.OkHttpClient

/**
 * Uses certificates accepted in the IDE, including university and corporate roots.
 * The JVM default trust store does not include certificates accepted through IDE settings.
 */
fun OkHttpClient.Builder.trustIdeCertificates(): OkHttpClient.Builder {
    val certificates = CertificateManager.getInstance()
    return sslSocketFactory(certificates.sslContext.socketFactory, certificates.trustManager)
}
