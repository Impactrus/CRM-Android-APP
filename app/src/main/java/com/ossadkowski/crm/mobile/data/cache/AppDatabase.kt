package com.ossadkowski.crm.mobile.data.cache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "crm_cache.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cache_entries (
                cache_key TEXT PRIMARY KEY,
                json_data TEXT NOT NULL,
                cached_at INTEGER NOT NULL,
                ttl_ms INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pending_actions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                action_type TEXT NOT NULL,
                payload TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                status TEXT DEFAULT 'pending',
                retry_count INTEGER DEFAULT 0,
                error_message TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS pending_actions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    action_type TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    status TEXT DEFAULT 'pending',
                    retry_count INTEGER DEFAULT 0,
                    error_message TEXT
                )
            """)
        }
    }

    // ── Cache operations ──

    suspend fun getValid(key: String): CacheEntry? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cursor = readableDatabase.rawQuery(
            "SELECT cache_key, json_data, cached_at, ttl_ms FROM cache_entries WHERE cache_key = ? AND (cached_at + ttl_ms) > ?",
            arrayOf(key, now.toString())
        )
        cursor.use {
            if (it.moveToFirst()) CacheEntry(it.getString(0), it.getString(1), it.getLong(2), it.getLong(3))
            else null
        }
    }

    /** Returns cached data even if expired (stale). Used as offline fallback. */
    suspend fun getAny(key: String): CacheEntry? = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT cache_key, json_data, cached_at, ttl_ms FROM cache_entries WHERE cache_key = ?",
            arrayOf(key)
        )
        cursor.use {
            if (it.moveToFirst()) CacheEntry(it.getString(0), it.getString(1), it.getLong(2), it.getLong(3))
            else null
        }
    }

    suspend fun put(entry: CacheEntry) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("cache_key", entry.cache_key)
            put("json_data", entry.json_data)
            put("cached_at", entry.cached_at)
            put("ttl_ms", entry.ttl_ms)
        }
        writableDatabase.insertWithOnConflict("cache_entries", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun invalidateByPrefix(prefix: String) = withContext(Dispatchers.IO) {
        writableDatabase.delete("cache_entries", "cache_key LIKE ?", arrayOf("$prefix%"))
    }

    suspend fun invalidate(key: String) = withContext(Dispatchers.IO) {
        writableDatabase.delete("cache_entries", "cache_key = ?", arrayOf(key))
    }

    suspend fun evictExpired() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        writableDatabase.delete("cache_entries", "(cached_at + ttl_ms) < ?", arrayOf(now.toString()))
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        writableDatabase.delete("cache_entries", null, null)
        writableDatabase.delete("pending_actions", null, null)
    }

    // ── Pending actions operations ──

    suspend fun enqueueAction(actionType: String, payload: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("action_type", actionType)
            put("payload", payload)
            put("created_at", System.currentTimeMillis())
            put("status", "pending")
        }
        writableDatabase.insert("pending_actions", null, values)
    }

    suspend fun getPendingActions(): List<PendingAction> = withContext(Dispatchers.IO) {
        val actions = mutableListOf<PendingAction>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, action_type, payload, created_at, retry_count FROM pending_actions WHERE status = 'pending' ORDER BY created_at ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                actions.add(PendingAction(it.getLong(0), it.getString(1), it.getString(2), it.getLong(3), it.getInt(4)))
            }
        }
        actions
    }

    suspend fun markActionDone(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("pending_actions", "id = ?", arrayOf(id.toString()))
    }

    suspend fun markActionFailed(id: Long, error: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("retry_count", "retry_count + 1")
            put("error_message", error)
        }
        // Use raw SQL for increment
        writableDatabase.execSQL(
            "UPDATE pending_actions SET retry_count = retry_count + 1, error_message = ? WHERE id = ?",
            arrayOf(error, id)
        )
    }

    suspend fun removeFailedActions(maxRetries: Int = 5) = withContext(Dispatchers.IO) {
        writableDatabase.delete("pending_actions", "retry_count >= ?", arrayOf(maxRetries.toString()))
    }

    suspend fun getPendingCount(): Int = withContext(Dispatchers.IO) {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM pending_actions WHERE status = 'pending'", null
        )
        cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDatabase(context).also { INSTANCE = it }
            }
    }
}

data class PendingAction(
    val id: Long,
    val actionType: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int
)
