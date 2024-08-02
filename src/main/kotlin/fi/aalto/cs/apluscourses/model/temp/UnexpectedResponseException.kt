package fi.aalto.cs.apluscourses.model.temp

import org.apache.http.HttpResponse
import java.io.IOException
import java.io.Serial

class UnexpectedResponseException(
    response: HttpResponse,
    message: String
) : IOException(message) {
    @Transient
    private val response: HttpResponse

    /**
     * Constructs a [UnexpectedResponseException] with the given response and message.
     *
     * @param response The response to which this exception relates.
     * @param message  The message.
     */
    init {
        this.response = response
    }

    fun getResponse(): HttpResponse {
        return response
    }

    companion object {
        @Serial
        private val serialVersionUID = -3010286248078758468L
    }
}
