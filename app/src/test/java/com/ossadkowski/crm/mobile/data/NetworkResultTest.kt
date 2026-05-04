package com.ossadkowski.crm.mobile.data

import org.junit.Assert.*
import org.junit.Test

class NetworkResultTest {

    @Test
    fun `Success holds data`() {
        val result = NetworkResult.Success("hello")
        assertEquals("hello", result.data)
        assertNull(result.message)
    }

    @Test
    fun `Error holds message and optional data`() {
        val result = NetworkResult.Error<String>("fail", "fallback")
        assertEquals("fail", result.message)
        assertEquals("fallback", result.data)
    }

    @Test
    fun `Error without data`() {
        val result = NetworkResult.Error<String>("fail")
        assertEquals("fail", result.message)
        assertNull(result.data)
    }

    @Test
    fun `Loading has no data or message`() {
        val result = NetworkResult.Loading<String>()
        assertNull(result.data)
        assertNull(result.message)
    }

    @Test
    fun `is check with Success`() {
        val result: NetworkResult<Int> = NetworkResult.Success(42)
        assertTrue(result is NetworkResult.Success)
        assertFalse(result is NetworkResult.Error)
        assertFalse(result is NetworkResult.Loading)
    }

    @Test
    fun `is check with Error`() {
        val result: NetworkResult<Int> = NetworkResult.Error("err")
        assertFalse(result is NetworkResult.Success)
        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `is check with Loading`() {
        val result: NetworkResult<Int> = NetworkResult.Loading()
        assertTrue(result is NetworkResult.Loading)
    }
}
