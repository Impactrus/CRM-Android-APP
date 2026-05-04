package com.ossadkowski.crm.mobile.data.cache

data class CacheEntry(
    val cache_key: String,
    val json_data: String,
    val cached_at: Long,
    val ttl_ms: Long
)
