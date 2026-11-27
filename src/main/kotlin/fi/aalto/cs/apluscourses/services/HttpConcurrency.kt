package fi.aalto.cs.apluscourses.services

import okhttp3.Dispatcher
import okhttp3.OkHttpClient

// OkHttp defaults to five requests per host. Slow downloads can delay API refreshes.
private const val MAX_REQUESTS_PER_HOST = 32

/** Raises the per-host request limit to [MAX_REQUESTS_PER_HOST]. */
fun OkHttpClient.Builder.allowConcurrentRequests(): OkHttpClient.Builder =
    dispatcher(Dispatcher().apply { maxRequestsPerHost = MAX_REQUESTS_PER_HOST })
