package com.fourgeailabs.neuropath.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Network hardening for [LlamaClient]:
 * - [LlamaClient.classifyNetworkError] distinguishes no-network, timeout,
 *   server-error, auth, and client-error failures (walking the causal chain,
 *   since Retrofit/OkHttp wrap the raw IOException);
 * - [LlamaClient.withNetworkRetry] retries transient failures with bounded
 *   exponential backoff, and never retries auth failures, client errors, or
 *   coroutine cancellation;
 * - [LlamaClient.safeMessage] redacts bearer-shaped tokens from log text.
 */
class LlamaClientNetworkTest {

    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "err".toResponseBody("text/plain".toMediaType())))

    @Test
    fun classify_unknownHost_isNoNetwork() {
        assertEquals(
            LlamaClient.NetworkErrorKind.NO_NETWORK,
            LlamaClient.classifyNetworkError(UnknownHostException("dns"))
        )
    }

    @Test
    fun classify_noRoute_isNoNetwork() {
        assertEquals(
            LlamaClient.NetworkErrorKind.NO_NETWORK,
            LlamaClient.classifyNetworkError(NoRouteToHostException("route"))
        )
    }

    @Test
    fun classify_connectException_isNoNetwork() {
        assertEquals(
            LlamaClient.NetworkErrorKind.NO_NETWORK,
            LlamaClient.classifyNetworkError(ConnectException("refused"))
        )
    }

    @Test
    fun classify_socketTimeout_isTimeout() {
        assertEquals(
            LlamaClient.NetworkErrorKind.TIMEOUT,
            LlamaClient.classifyNetworkError(SocketTimeoutException("read timed out"))
        )
    }

    @Test
    fun classify_http401_isAuth() {
        assertEquals(LlamaClient.NetworkErrorKind.AUTH, LlamaClient.classifyNetworkError(httpException(401)))
    }

    @Test
    fun classify_http403_isAuth() {
        assertEquals(LlamaClient.NetworkErrorKind.AUTH, LlamaClient.classifyNetworkError(httpException(403)))
    }

    @Test
    fun classify_http500_isServerError() {
        assertEquals(
            LlamaClient.NetworkErrorKind.SERVER_ERROR,
            LlamaClient.classifyNetworkError(httpException(500))
        )
    }

    @Test
    fun classify_http429_isClientError_notRetried() {
        assertEquals(
            LlamaClient.NetworkErrorKind.CLIENT_ERROR,
            LlamaClient.classifyNetworkError(httpException(429))
        )
    }

    @Test
    fun classify_wrappedIOException_unwrapsCause() {
        val wrapped = RuntimeException("retrofit wrapper", UnknownHostException("dns"))
        assertEquals(LlamaClient.NetworkErrorKind.NO_NETWORK, LlamaClient.classifyNetworkError(wrapped))
    }

    @Test
    fun classify_unknown_isUnknown() {
        assertEquals(
            LlamaClient.NetworkErrorKind.UNKNOWN,
            LlamaClient.classifyNetworkError(IllegalStateException("weird"))
        )
    }

    @Test
    fun retry_transientTimeoutThenSuccess_returnsValue() = runBlocking {
        var calls = 0
        val result = LlamaClient.withNetworkRetry(maxAttempts = 3, initialDelayMs = 0) {
            calls++
            if (calls < 3) throw SocketTimeoutException("slow server")
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, calls)
    }

    @Test
    fun retry_serverError_isRetried() = runBlocking {
        var calls = 0
        val result = LlamaClient.withNetworkRetry(maxAttempts = 3, initialDelayMs = 0) {
            calls++
            if (calls == 1) throw httpException(503)
            "recovered"
        }
        assertEquals("recovered", result)
        assertEquals(2, calls)
    }

    @Test
    fun retry_authFailure_isNeverRetried() = runBlocking {
        var calls = 0
        try {
            LlamaClient.withNetworkRetry(maxAttempts = 3, initialDelayMs = 0) {
                calls++
                throw httpException(401)
            }
            fail("auth failure must propagate")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }
        assertEquals("auth failures must not be retried", 1, calls)
    }

    @Test
    fun retry_clientError_isNeverRetried() = runBlocking {
        var calls = 0
        try {
            LlamaClient.withNetworkRetry(maxAttempts = 3, initialDelayMs = 0) {
                calls++
                throw httpException(429)
            }
            fail("client error must propagate")
        } catch (_: HttpException) {
        }
        assertEquals(1, calls)
    }

    @Test
    fun retry_cancellation_isNeverRetried() = runBlocking {
        var calls = 0
        try {
            LlamaClient.withNetworkRetry(maxAttempts = 3, initialDelayMs = 0) {
                calls++
                throw CancellationException("gone")
            }
            fail("cancellation must propagate")
        } catch (_: CancellationException) {
        }
        assertEquals(1, calls)
    }

    @Test
    fun retry_respectsMaxAttempts() = runBlocking {
        var calls = 0
        try {
            LlamaClient.withNetworkRetry(maxAttempts = 2, initialDelayMs = 0) {
                calls++
                throw SocketTimeoutException("always slow")
            }
            fail("must give up after maxAttempts")
        } catch (_: SocketTimeoutException) {
        }
        assertEquals(2, calls)
    }

    @Test
    fun retry_nonRetryableUnknown_isNotRetried() = runBlocking {
        var calls = 0
        try {
            LlamaClient.withNetworkRetry(maxAttempts = 3, initialDelayMs = 0) {
                calls++
                throw IllegalArgumentException("bad input")
            }
            fail("must propagate")
        } catch (_: IllegalArgumentException) {
        }
        assertEquals(1, calls)
    }

    @Test
    fun safeMessage_redactsBearerToken() {
        val redacted = LlamaClient.safeMessage(RuntimeException("call failed, Bearer hf_abcDEF123.xyz-~_+/, retry later"))
        assertTrue("token must be redacted, was: $redacted", !redacted.contains("hf_abcDEF123"))
        assertTrue(redacted.contains("Bearer [redacted]"))
    }

    @Test
    fun safeMessage_keepsPlainMessages() {
        assertEquals("plain failure", LlamaClient.safeMessage(RuntimeException("plain failure")))
    }
}
