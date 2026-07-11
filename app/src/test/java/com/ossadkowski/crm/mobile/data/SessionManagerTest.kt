package com.ossadkowski.crm.mobile.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.lang.reflect.Field

/**
 * Tests SessionManager logic by injecting a mock SharedPreferences.
 * We bypass the EncryptedSharedPreferences constructor via reflection.
 */
class SessionManagerTest {

    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var sessionManager: SessionManager
    private val prefsStore = mutableMapOf<String, Any?>()

    @Before
    fun setup() {
        mockEditor = mock {
            on { putString(any(), anyOrNull()) } doAnswer { invocation ->
                prefsStore[invocation.getArgument(0)] = invocation.getArgument<String?>(1)
                mockEditor
            }
            on { putInt(any(), any()) } doAnswer { invocation ->
                prefsStore[invocation.getArgument(0)] = invocation.getArgument<Int>(1)
                mockEditor
            }
            on { putStringSet(any(), anyOrNull()) } doAnswer { invocation ->
                prefsStore[invocation.getArgument(0)] = invocation.getArgument<Set<String>?>(1)
                mockEditor
            }
            on { clear() } doAnswer {
                prefsStore.clear()
                mockEditor
            }
            on { apply() } doAnswer { /* no-op */ }
        }

        mockPrefs = mock {
            on { edit() } doReturn mockEditor
            on { getString(any(), anyOrNull()) } doAnswer { invocation ->
                prefsStore[invocation.getArgument(0)] as? String ?: invocation.getArgument(1)
            }
            on { getInt(any(), any()) } doAnswer { invocation ->
                prefsStore[invocation.getArgument(0)] as? Int ?: invocation.getArgument(1)
            }
            @Suppress("UNCHECKED_CAST")
            on { getStringSet(any(), anyOrNull()) } doAnswer { invocation ->
                prefsStore[invocation.getArgument(0)] as? Set<String> ?: invocation.getArgument(1)
            }
        }

        // Create SessionManager bypassing EncryptedSharedPreferences init
        val mockContext = mock<Context>()
        sessionManager = createSessionManagerWithMockPrefs(mockContext, mockPrefs)
    }

    private fun createSessionManagerWithMockPrefs(context: Context, prefs: SharedPreferences): SessionManager {
        // Use Unsafe or allocateInstance to bypass constructor
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateMethod = unsafeClass.getMethod("allocateInstance", Class::class.java)
        val instance = allocateMethod.invoke(unsafe, SessionManager::class.java) as SessionManager

        // Set the prefs field via reflection
        val prefsField: Field = SessionManager::class.java.getDeclaredField("prefs")
        prefsField.isAccessible = true
        prefsField.set(instance, prefs)

        return instance
    }

    @Test
    fun `saveSession stores all fields`() {
        sessionManager.saveSession("token123", 42, "Admin", "admin", "IT", 99)

        assertEquals("token123", prefsStore["token"])
        assertEquals(42, prefsStore["userId"])
        assertEquals("Admin", prefsStore["role"])
        assertEquals("admin", prefsStore["username"])
        assertEquals("IT", prefsStore["dzial"])
        assertEquals(99, prefsStore["employeeCacheId"])
    }

    @Test
    fun `saveSession with null dzial stores empty string`() {
        sessionManager.saveSession("t", 1, "User", "u", null, null)
        assertEquals("", prefsStore["dzial"])
        assertEquals(0, prefsStore["employeeCacheId"])
    }

    @Test
    fun `clear removes all fields`() {
        prefsStore["token"] = "abc"
        prefsStore["userId"] = 1
        sessionManager.clear()
        assertTrue(prefsStore.isEmpty())
    }

    @Test
    fun `isLoggedIn returns true when token exists`() {
        prefsStore["token"] = "valid_token"
        assertTrue(sessionManager.isLoggedIn)
    }

    @Test
    fun `isLoggedIn returns false when no token`() {
        assertFalse(sessionManager.isLoggedIn)
    }

    @Test
    fun `token getter returns stored token`() {
        prefsStore["token"] = "mytoken"
        assertEquals("mytoken", sessionManager.token)
    }

    @Test
    fun `token getter returns null when empty`() {
        assertNull(sessionManager.token)
    }

    @Test
    fun `updateToken updates only token`() {
        prefsStore["token"] = "old"
        prefsStore["userId"] = 42
        sessionManager.updateToken("new_token")
        assertEquals("new_token", prefsStore["token"])
        assertEquals(42, prefsStore["userId"])  // other fields unchanged
    }

    @Test
    fun `userId returns 0 when no session`() {
        assertEquals(0, sessionManager.userId)
    }

    @Test
    fun `role returns User as default`() {
        assertEquals("User", sessionManager.role)
    }

    @Test
    fun `username returns empty string as default`() {
        assertEquals("", sessionManager.username)
    }

    @Test
    fun `dzial returns empty string as default`() {
        assertEquals("", sessionManager.dzial)
    }

    @Test
    fun `employeeCacheId returns 0 as default`() {
        assertEquals(0, sessionManager.employeeCacheId)
    }

    @Test
    fun `getters return saved values`() {
        sessionManager.saveSession("t", 5, "Manager", "mgr", "Sales", 77)
        assertEquals(5, sessionManager.userId)
        assertEquals("Manager", sessionManager.role)
        assertEquals("mgr", sessionManager.username)
        assertEquals("Sales", sessionManager.dzial)
        assertEquals(77, sessionManager.employeeCacheId)
    }
}
