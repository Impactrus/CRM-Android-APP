package com.ossadkowski.crm.callhistory

import android.content.Context
import android.location.Geocoder
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class OrganizerManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("organizer_prefs", Context.MODE_PRIVATE)

    fun getItems(): List<OrganizerItem> {
        val jsonStr = prefs.getString("items", "[]") ?: "[]"
        val items = mutableListOf<OrganizerItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                items.add(
                    OrganizerItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        address = obj.getString("address"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        lastVisitNote = obj.optString("lastVisitNote", ""),
                        visitedAt = obj.optLong("visitedAt", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    fun saveItems(items: List<OrganizerItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("address", item.address)
                put("latitude", item.latitude)
                put("longitude", item.longitude)
                put("lastVisitNote", item.lastVisitNote)
                put("visitedAt", item.visitedAt)
            }
            arr.put(obj)
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    fun updateNote(id: String, note: String) {
        val items = getItems().map {
            if (it.id == id) {
                it.copy(lastVisitNote = note, visitedAt = System.currentTimeMillis())
            } else {
                it
            }
        }
        saveItems(items)
    }

    fun syncFromAddressBook(addressBookJson: String) {
        // Parse the list of addresses from JSON (returned by CRM API /api/kontrahenci)
        val newItems = mutableListOf<OrganizerItem>()
        try {
            val dataArr = if (addressBookJson.trim().startsWith("[")) {
                JSONArray(addressBookJson)
            } else {
                val root = JSONObject(addressBookJson)
                root.optJSONArray("data") ?: root.optJSONArray("items") ?: JSONArray()
            }
            val geocoder = Geocoder(context, Locale.getDefault())
            
            // Limit to max 50 since we have more space on map
            val limit = minOf(dataArr.length(), 50)
            for (i in 0 until limit) {
                val obj = dataArr.getJSONObject(i)
                val id = obj.optString("id", obj.optString("accountNum", i.toString()))
                val name = obj.optString("nazwa", obj.optString("name", "Klient $i"))
                val address = obj.optString("adres", obj.optString("address", ""))
                
                var lat = 51.107885 // Default Wrocław coordinates
                var lon = 17.038538
                
                if (address.isNotBlank()) {
                    try {
                        val addresses = geocoder.getFromLocationName(address, 1)
                        if (!addresses.isNullOrEmpty()) {
                            lat = addresses[0].latitude
                            lon = addresses[0].longitude
                        } else {
                            // Safe mock offset for testing so different clients have different locations
                            lat += (i * 0.005)
                            lon += (i * 0.005)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        lat += (i * 0.005)
                        lon += (i * 0.005)
                    }
                }
                
                newItems.add(
                    OrganizerItem(
                        id = id,
                        name = name,
                        address = address,
                        latitude = lat,
                        longitude = lon
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Merge notes from existing items if they match
        val existingItems = getItems().associateBy { it.id }
        val mergedItems = newItems.map { newItem ->
            val existing = existingItems[newItem.id]
            if (existing != null) {
                newItem.copy(
                    lastVisitNote = existing.lastVisitNote,
                    visitedAt = existing.visitedAt
                )
            } else {
                newItem
            }
        }
        
        saveItems(mergedItems)
    }
}
